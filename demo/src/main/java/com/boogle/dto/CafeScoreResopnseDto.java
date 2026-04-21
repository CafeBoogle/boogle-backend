package com.boogle.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CafeScoreResopnseDto {

    private Long cafeId;
    private int reviewCount;

    // 그래프 점수
    private Double toiletScoreAvg;
    private Double outletScoreAvg;
    private Double seatScoreAvg;
    private Double wifiScoreAvg;
    private Double noiseScoreAvg;
    private Double studyScoreAvg;
}
