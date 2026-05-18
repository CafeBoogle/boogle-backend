package com.boogle.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {
    private Long cafeId;
    private String shortReview;

    // 육각형 차트 점수들
    private Integer toiletScore;
    private Integer outletScore;
    private Integer seatScore;
    private Integer wifiScore;
    private Integer noiseScore;
    private Integer studyScore;

    // 삭제할 이미지 아이디
    private List<Long> deleteImageIds;
}
