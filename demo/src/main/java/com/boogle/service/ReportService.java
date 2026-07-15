package com.boogle.service;

import com.boogle.entity.Report;
import com.boogle.entity.Review;
import com.boogle.entity.User;
import com.boogle.entity.type.ReportReason;
import com.boogle.entity.type.ReportStatus;
import com.boogle.repository.ReportRepository;
import com.boogle.repository.ReviewRepository;
import com.boogle.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // 누적 신고 수 갯수가 다 차면 자동 블라인드
    private static final int AUTO_BLIND_THRESHOLD = 5;

    @Transactional
    public void reportReview(Long reviewId, Long reporterId, ReportReason reason, String detail) {
        if (reportRepository.existsByReviewIdAndReporterId(reviewId, reporterId)) {
            throw new IllegalArgumentException("이미 신고한 리뷰입니다.");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Report report = Report.builder()
                .review(review)
                .reporter(reporter)
                .reason(reason)
                .detail(detail)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);

        // 누적 신고 수 최대치면 자동 블라인드
        long pendingCount = reportRepository.countByReviewIdAndStatus(reviewId, ReportStatus.PENDING);
        if (pendingCount >= AUTO_BLIND_THRESHOLD) {
            review.blind();
        }
    }

    @Transactional
    public void resolveReport(Long reportId, ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("신고 내역을 찾을 수 없습니다."));

        if (newStatus == ReportStatus.RESOLVED) {
            report.getReview().blind();
        }
        report.updateStatus(newStatus);
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING);
    }

}
