package com.boogle.service;

import com.boogle.dto.CafeDetailResponseDto;
import com.boogle.dto.CafeResponseDto;
import com.boogle.dto.CafeSaveRequestDto;
import com.boogle.dto.CafeScoreResopnseDto;
import com.boogle.dto.projection.CafeScoreProjection;
import com.boogle.entity.Cafe;
import com.boogle.repository.CafeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeService {
    private final CafeRepository cafeRepository;
    private final ReviewService reviewService;

    // 카카오맵 범위 내에 있는 카페를 목록화
    public List<CafeResponseDto> findCafesWithinBounds(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        List<Cafe> cafes = cafeRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);

        // Entity를 Dto로 변환
        return cafes.stream().map(cafe -> CafeResponseDto.builder()
                .id(cafe.getId())
                .name(cafe.getName())
                .address(cafe.getAddress())
                .latitude(cafe.getLatitude())
                .longitude(cafe.getLongitude())
                .thumbnail(cafe.getImageName())
                .build())
                .collect(Collectors.toList());

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
    
    // 카페 상세와 그래프 점수 통합
    @Transactional(readOnly = true)
    public CafeDetailResponseDto getCafeDetail(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페가 존재하지 않습니다."));

        CafeScoreResopnseDto scores = reviewService.getCafeScore(cafeId);

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
                .build();
    }

    public List<CafeResponseDto> getSortedCafes(List<CafeResponseDto> cafes, List<String> selectedTags) {
        if (selectedTags == null || selectedTags.isEmpty()) {
            return cafes;
        }

        return cafes.stream()
                .sorted((c1, c2) -> {
                    CafeScoreResopnseDto s1 = c1.getScore();
                    CafeScoreResopnseDto s2 = c2.getScore();

                    // 데이터가 없는 카페는 뒤로 밀기
                    if (s1 == null) return 1;
                    if (s2 == null) return -1;

                    // 1순위: 선택된 태그 점수 합계 (내림차순)
                    double selected1 = calculateSelectedSum(s1, selectedTags);
                    double selected2 = calculateSelectedSum(s2, selectedTags);
                    if (selected1 != selected2) return Double.compare(selected2, selected1);

                    // 2순위: 전체 6개 항목 점수 총합 (동점자 처리)
                    double total1 = calculateAllSum(s1);
                    double total2 = calculateAllSum(s2);
                    if (total1 != total2) return Double.compare(total2, total1);

                    // 3순위: 리뷰 개수 (최종 동점자 처리)
                    return Integer.compare(s2.getReviewCount(), s1.getReviewCount());
                })
                .collect(Collectors.toList());
    }

    // 사용자가 선택한 태그만 더하기
    private double calculateSelectedSum(CafeScoreResopnseDto s, List<String> tags) {
        double sum = 0.0;
        if (tags.contains("toilet")) sum += nvl(s.getToiletScoreAvg());
        if (tags.contains("outlet")) sum += nvl(s.getOutletScoreAvg());
        if (tags.contains("seat"))   sum += nvl(s.getSeatScoreAvg());
        if (tags.contains("wifi"))   sum += nvl(s.getWifiScoreAvg());
        if (tags.contains("noise"))  sum += nvl(s.getNoiseScoreAvg());
        if (tags.contains("openTime")) sum += nvl(s.getOpenTimeScoreAvg());
        return sum;
    }

    // 전체 평점 합산
    private double calculateAllSum(CafeScoreResopnseDto s) {
        return nvl(s.getToiletScoreAvg()) + nvl(s.getOutletScoreAvg()) + nvl(s.getSeatScoreAvg()) +
                nvl(s.getWifiScoreAvg()) + nvl(s.getNoiseScoreAvg()) + nvl(s.getOpenTimeScoreAvg());
    }

    // Null 방지 보조 메서드
    private double nvl(Double val) {
        return (val == null) ? 0.0 : val;
    }
}


