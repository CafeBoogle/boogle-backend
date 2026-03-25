package com.boogle.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private Long cafeId;
    private String shortReview;
    private String imageName;

    // 육각형 차트 점수들
    private Integer toiletScore;
    private Integer outletScore;
    private Integer seatScore;
    private Integer wifiScore;
    private Integer noiseScore;
}
