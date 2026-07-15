package com.boogle.repository;

import com.boogle.entity.Report;
import com.boogle.entity.type.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReviewIdAndReporterId(Long reviewId, Long reporterId);
    List<Report> findByStatus(ReportStatus status);
    long countByReviewIdAndStatus(Long reviewId, ReportStatus status);
}
