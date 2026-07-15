package com.boogle.dto;

import com.boogle.entity.Report;
import com.boogle.entity.type.ReportReason;
import com.boogle.entity.type.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponseDto {
    private Long id;
    private Long reviewId;
    private String reviewContent; // 신고당한 리뷰 내용
    private Long reporterId;
    private String reporterNickname;
    private ReportReason reason;
    private String detail;
    private ReportStatus status;
    private LocalDateTime createdAt;

    public static ReportResponseDto from(Report report) {
        return ReportResponseDto.builder()
                .id(report.getId())
                .reviewId(report.getReview().getId())
                .reviewContent(report.getReview().getShortReview())
                .reporterId(report.getReporter().getId())
                .reporterNickname(report.getReporter().getNickname())
                .reason(report.getReason())
                .detail(report.getDetail())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
