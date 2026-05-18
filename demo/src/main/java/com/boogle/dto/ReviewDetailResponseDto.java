package com.boogle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewDetailResponseDto {

    private Long cafeId;
    private String shortReview;

    private Integer outletScore;
    private Integer seatScore;
    private Integer toiletScore;
    private Integer wifiScore;
    private Integer noiseScore;
    private Integer studyScore;

    private List<String> imageUrls;
}
