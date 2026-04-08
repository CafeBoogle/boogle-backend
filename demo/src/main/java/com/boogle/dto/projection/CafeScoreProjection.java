package com.boogle.dto.projection;

public record CafeScoreProjection(
        Double toiletScoreAvg,
        Double outletScoreAvg,
        Double seatScoreAvg,
        Double wifiScoreAvg,
        Double noiseScoreAvg,
        Long reviewCount

){}
