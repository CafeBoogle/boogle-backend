package com.boogle.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private Long cafeId;
    private String shortReview;

    private Integer outletScore;
    private Integer seatScore;
    private Integer noiseScore;
    private Integer toiletScore;
    private Integer wifiScore;
}