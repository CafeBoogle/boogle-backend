package com.boogle.dto;

import com.boogle.dto.projection.ShortReviewProjection;
import com.boogle.dto.request.ReviewRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CafeDetailResponseDto {

    // 카페 기본 정보
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String contact;
    private String placeId;
    private List<ShortReviewProjection> shortReviews;
    
    // 리뷰 점수
    private CafeScoreResopnseDto score;

    // 태그
    private List<String> tags;
    
    // 리뷰 이미지
    @JsonProperty("imageName")
    private List<String> reviewImageUrls;
}
