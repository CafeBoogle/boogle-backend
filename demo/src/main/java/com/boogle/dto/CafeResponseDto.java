package com.boogle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CafeResponseDto {
    private Long id; // 우리 DB에 들어온 카페 고유 ID
    private String name; // 카페 이름
    private String address; // 카페 주소
    private Double latitude; // 위도(y)
    private Double longitude; // 경도(경찰과 도둑 아님 X)
    private String thumbnail; // 카페 대표 이미지 URL
    private String kakaoPlaceId; // 카카오맵에서 카페 고유 id
    private CafeScoreResopnseDto score;
}
