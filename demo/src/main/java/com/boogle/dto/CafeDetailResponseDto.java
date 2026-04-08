package com.boogle.dto;

import com.boogle.dto.request.ReviewRequest;
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
    private String imageName;
    private String contact;
    private String placeId;
    
    // 리뷰 점수
    private CafeScoreResopnseDto score;
}
