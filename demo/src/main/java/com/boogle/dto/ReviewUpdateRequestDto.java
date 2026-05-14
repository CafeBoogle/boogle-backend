package com.boogle.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ReviewUpdateRequestDto {

    private String shortReview;

    private Integer toiletScore;
    private Integer outletScore;
    private Integer seatScore;
    private Integer wifiScore;
    private Integer noiseScore;
    private Integer studyScore;

    private List<String> imageUrls;
}
