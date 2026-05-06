package com.boogle.service;

import com.boogle.dto.CafeDetailResponseDto;
import com.boogle.dto.CafeResponseDto;
import com.boogle.dto.CafeSaveRequestDto;
import com.boogle.dto.CafeScoreResopnseDto;
import com.boogle.dto.projection.CafeListProjection;
import com.boogle.dto.projection.CafeScoreProjection;
import com.boogle.entity.Cafe;
import com.boogle.repository.CafeRepository;
import com.boogle.util.CafeTagGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeService {
    private final CafeRepository cafeRepository;
    private final ReviewService reviewService;
    private final CafeTagGenerator cafeTagGenerator;
    // 태그를 나타내는 점수 기준
    private static final double TAG_THRESHOLD = 3.5;
    // 카카오맵 범위 내에 있는 카페를 목록화
    public List<CafeResponseDto> findCafesWithinBounds(Double minLat, Double maxLat, Double minLng, Double maxLng) {

        List<CafeListProjection> cafes = cafeRepository.findCafeListWithinBounds(minLat, maxLat, minLng, maxLng);

        return cafes.stream().map(c -> {

            int reviewCount = c.getReviewCount().intValue();

            List<String> tags = reviewCount == 0
                    ? List.of()
                    : List.of("리뷰 있음");

            return CafeResponseDto.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .address(c.getAddress())
                    .latitude(c.getLatitude())
                    .longitude(c.getLongitude())
                    .thumbnail(c.getThumbnail())
                    .tags(tags)
                    .build();
        }).toList();
    }


    // 카페 목록에서 카페를 클릭 시 우리 DB에 저장하는 로직
    @Transactional
    public long getOrCreateCafe(CafeSaveRequestDto dto) {
        // kakaoPlaceId로 DB를 조회
        // if (이미 존재할 경우) 객체를 그대로 반환
        // else DB에 저장 후 반환
        Cafe cafe = cafeRepository.findByKakaoPlaceId(dto.getKakaoPlaceId())
                .orElseGet(() -> cafeRepository.save(dto.toEntity()));

        // 우리 DB에 저장된 고유식별자(ID)를 프론트로 넘김
        return cafe.getId();
    }

    @Transactional(readOnly = true)
    public Map<String, CafeResponseDto> findCafesByKakaoIds(List<String> kakaoIds) {
        List<Cafe> cafes = cafeRepository.findByKakaoPlaceIdIn(kakaoIds);

        return cafes.stream().collect(Collectors.toMap(
                Cafe::getKakaoPlaceId,
                cafe -> {
                    CafeScoreResopnseDto dto = reviewService.getCafeScore(cafe.getId());

                    return CafeResponseDto.builder()
                            .id(cafe.getId())
                            .kakaoPlaceId(cafe.getKakaoPlaceId())
                            .name(cafe.getName())
                            .address(cafe.getAddress())
                            .latitude(cafe.getLatitude())
                            .longitude(cafe.getLongitude())
                            .score(dto)
                            .tags(cafeTagGenerator.generateTags(dto))
                            .build();
                }
        ));
    }
    
    // 카페 상세와 그래프 점수 통합
    @Transactional(readOnly = true)
    public CafeDetailResponseDto getCafeDetail(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페가 존재하지 않습니다."));

        CafeScoreResopnseDto scores = reviewService.getCafeScore(cafeId);
        List<String> tags = cafeTagGenerator.generateTags(scores);

        return CafeDetailResponseDto.builder()
                .id(cafe.getId())
                .name(cafe.getName())
                .address(cafe.getAddress())
                .latitude(cafe.getLatitude())
                .longitude(cafe.getLongitude())
                .imageName(cafe.getImageName())
                .contact(cafe.getContact())
                .placeId(cafe.getKakaoPlaceId())
                .score(scores)
                .tags(tags)
                .build();
    }

    public List<CafeResponseDto> sortCafesByTags(List<CafeResponseDto> cafes, List<String> selectedTags) {
        if (cafes == null || cafes.isEmpty()) return cafes;
        if (selectedTags == null || selectedTags.isEmpty()) return cafes;

        return cafes.stream()
                .sorted((c1, c2) -> {
                    CafeScoreResopnseDto s1 = c1.getScore();
                    CafeScoreResopnseDto s2 = c2.getScore();

                    // 점수 데이터가 없는 카페는 리스트의 맨 뒤로 보냄
                    if (s1 == null && s2 == null) return 0;
                    if (s1 == null) return 1;
                    if (s2 == null) return -1;

                    // [1단계] 사용자가 선택한 태그들의 점수 합계 비교
                    double selectedSum1 = calculateSum(s1, selectedTags);
                    double selectedSum2 = calculateSum(s2, selectedTags);
                    if (selectedSum1 != selectedSum2) {
                        return Double.compare(selectedSum2, selectedSum1); // 내림차순
                    }

                    // [2단계] 동점일 경우, 카페의 전체 항목(6개) 점수 합계 비교
                    double totalSum1 = calculateAllSum(s1);
                    double totalSum2 = calculateAllSum(s2);
                    if (totalSum1 != totalSum2) {
                        return Double.compare(totalSum2, totalSum1);
                    }

                    // [3단계] 여전히 동점일 경우, 리뷰 개수가 많은 순으로 정렬
                    return Integer.compare(s2.getReviewCount(), s1.getReviewCount());
                })
                .collect(Collectors.toList());
    }

    private double calculateSum(CafeScoreResopnseDto s, List<String> tags) {
        double sum = 0.0;
        if (tags.contains("toilet"))   sum += nvl(s.getToiletScoreAvg());
        if (tags.contains("outlet"))   sum += nvl(s.getOutletScoreAvg());
        if (tags.contains("seat"))     sum += nvl(s.getSeatScoreAvg());
        if (tags.contains("wifi"))     sum += nvl(s.getWifiScoreAvg());
        if (tags.contains("noise"))    sum += nvl(s.getNoiseScoreAvg());
        if (tags.contains("study")) sum += nvl(s.getStudyScoreAvg());
        return sum;
    }

    // 전체 항목(6개) 무조건 다 합산
    private double calculateAllSum(CafeScoreResopnseDto s) {
        return nvl(s.getToiletScoreAvg()) + nvl(s.getOutletScoreAvg()) +
                nvl(s.getSeatScoreAvg()) + nvl(s.getWifiScoreAvg()) +
                nvl(s.getNoiseScoreAvg()) + nvl(s.getStudyScoreAvg());
    }

    // Null이면 0.0 반환
    private double nvl(Double val) {
        return (val == null) ? 0.0 : val;
    }
}


