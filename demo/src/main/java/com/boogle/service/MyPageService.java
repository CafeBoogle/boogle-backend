package com.boogle.service;

import com.boogle.dto.CafeScoreResopnseDto;
import com.boogle.dto.MyReviewResponseDto;
import com.boogle.dto.projection.CafeScoreProjection;
import com.boogle.repository.ReviewRepository;
import com.boogle.util.CafeTagGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final ReviewRepository reviewRepository;
    private final CafeTagGenerator cafeTagGenerator;

    public List<MyReviewResponseDto> getMyReviews(Long userId) {

        List<MyReviewResponseDto> reviews =
                reviewRepository.findMyReviews(userId);

        if (reviews.isEmpty()) {
            return reviews;
        }

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


            reviews.forEach(review -> {
                CafeScoreResopnseDto score = scoreMap.get(review.getCafeId());
                review.setTags(cafeTagGenerator.generateTags(score));
            });
        }
        return reviews;
    }
}
