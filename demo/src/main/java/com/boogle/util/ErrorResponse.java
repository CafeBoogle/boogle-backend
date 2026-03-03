package com.boogle.util;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    // 에러 발생시간
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // http 상태 코드
    private int status;

    // 에러 종류
    private String error;

    // 내가 작성한 아주 친절한 메세지
    private String message;

    // Valid에서 발생한 필드별 에러 메세지를 담는 맵
    private Map<String, String> validationErrors;
}
