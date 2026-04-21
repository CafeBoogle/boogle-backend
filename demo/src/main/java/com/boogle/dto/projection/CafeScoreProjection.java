package com.boogle.dto.projection;

public record CafeScoreProjection(
        Long cafeId,
        Double toiletScoreAvg,
        Double outletScoreAvg,
        Double seatScoreAvg,
        Double wifiScoreAvg,
        Double noiseScoreAvg,
        Double studyScoreAvg,
        Long reviewCount

){}
