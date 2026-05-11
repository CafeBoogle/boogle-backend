package com.boogle.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private Long cafeId;
    private String shortReview;

    // 육각형 차트 점수들
    private Integer toiletScore;
    private Integer outletScore;
    private Integer seatScore;
    private Integer wifiScore;
    private Integer noiseScore;
    private Integer studyScore;
}
