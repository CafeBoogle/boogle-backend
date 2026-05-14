package com.boogle.service;

import com.boogle.dto.CafeScoreResopnseDto;
import com.boogle.dto.MyReviewResponseDto;
import com.boogle.dto.projection.CafeScoreProjection;
import com.boogle.repository.ReviewRepository;
import com.boogle.util.CafeTagGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final ReviewRepository reviewRepository;
    private final CafeTagGenerator cafeTagGenerator;

    @Transactional(readOnly = true)
    public List<MyReviewResponseDto> getMyReviews(Long userId) {

        // raw 조회
        List<Object[]> rows =
                reviewRepository.findMyReviewsWithImagesRaw(userId);

        if (rows.isEmpty()) {
            return List.of();
        }

        // 리뷰 단위로 묶기
        Map<Long, MyReviewResponseDto> reviewMap = new LinkedHashMap<>();

        for (Object[] row : rows) {
            Long reviewId = (Long) row[0];

            reviewMap.computeIfAbsent(reviewId, id ->
                    new MyReviewResponseDto(
                            id,                     // review.id
                            (Long) row[1],          // cafe.id
                            (String) row[2],        // cafe.name
                            (String) row[3],        // cafe.address
                            (String) row[4],        // shortReview
                            null,                   // tags (아래에서 세팅)
                            new ArrayList<>(),       // imageUrls
                            (Integer) row[6],
                            (Integer) row[7],
                            (Integer) row[8],
                            (Integer) row[9],
                            (Integer) row[10],
                            (Integer) row[11]
                    )
            );

            // 이미지가 있을 때만 추가
            if (row[5] != null) {
                reviewMap.get(reviewId)
                        .getImageUrls()
                        .add((String) row[5]);
            }
        }

        List<MyReviewResponseDto> reviews =
                new ArrayList<>(reviewMap.values());

        // 카페별 점수 한 번에 조회
        List<Long> cafeIds = reviews.stream()
                .map(MyReviewResponseDto::getCafeId)
                .distinct()
                .toList();

        List<CafeScoreProjection> projections =
                reviewRepository.findCafeScoresByCafeIds(cafeIds);

        Map<Long, CafeScoreResopnseDto> scoreMap = new HashMap<>();

        for (CafeScoreProjection p : projections) {
            scoreMap.put(
                    p.cafeId(),
                    CafeScoreResopnseDto.builder()
                            .cafeId(p.cafeId())
                            .reviewCount(p.reviewCount().intValue())
                            .toiletScoreAvg(p.toiletScoreAvg())
                            .outletScoreAvg(p.outletScoreAvg())
                            .seatScoreAvg(p.seatScoreAvg())
                            .wifiScoreAvg(p.wifiScoreAvg())
                            .noiseScoreAvg(p.noiseScoreAvg())
                            .studyScoreAvg(p.studyScoreAvg())
                            .build()
            );
        }

        // 리뷰마다 태그 세팅
        for (MyReviewResponseDto review : reviews) {
            CafeScoreResopnseDto score = scoreMap.get(review.getCafeId());
            review.setTags(cafeTagGenerator.generateTags(score));
        }

        return reviews;
    }
}
