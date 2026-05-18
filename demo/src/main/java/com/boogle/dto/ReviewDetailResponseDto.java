package com.boogle.dto;

import com.boogle.entity.Cafe;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewDetailResponseDto {

    private Long cafeId;
    private String shortReview;
    private String cafeName;

    private Integer outletScore;
    private Integer seatScore;
    private Integer toiletScore;
    private Integer wifiScore;
    private Integer noiseScore;
    private Integer studyScore;

    private List<ImageDto> images;

    @Getter
    @AllArgsConstructor
    public static class ImageDto {
        private Long id;
        private String url;
    }
}
