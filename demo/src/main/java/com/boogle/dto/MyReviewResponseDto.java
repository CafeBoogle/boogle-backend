package com.boogle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyReviewResponseDto {

    private Long id;          // review.id
    private Long cafeId;    // 카페 ID
    private String name;      // 카페 이름
    private String address;   // 카페 주소
    @JsonProperty("comment")
    private String shortReview;   // 리뷰 내용
    @Setter
    private List<String> tags; // 태그
    List<String> imageUrls;

    // 내가 남긴 리뷰 점수
    private Integer toiletScore;
    private Integer outletScore;
    private Integer seatScore;
    private Integer wifiScore;
    private Integer noiseScore;
    private Integer studyScore;

}
