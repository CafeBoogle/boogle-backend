package com.boogle.controller;

import com.boogle.dto.ReportResponseDto;
import com.boogle.entity.Report;
import com.boogle.entity.type.ReportStatus;
import com.boogle.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getPending() {
        List<ReportResponseDto> result = reportService.getPendingReports().stream()
                .map(ReportResponseDto::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{reportId}")
    public ResponseEntity<Void> resolve(@PathVariable Long reportId, @RequestParam ReportStatus status) {
        reportService.resolveReport(reportId, status);
        return ResponseEntity.ok().build();
    }
}
