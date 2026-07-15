package com.boogle.controller;

import com.boogle.dto.ReportResponseDto;
import com.boogle.dto.request.ReportRequestDto;
import com.boogle.entity.User;
import com.boogle.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{reviewId}/report")
    public ResponseEntity<Void> report(@PathVariable Long reviewId,
                                       @AuthenticationPrincipal User user,
                                       @RequestBody ReportRequestDto dto){

        reportService.reportReview(reviewId, user.getId(), dto.getReason(), dto.getDetail());
        return ResponseEntity.ok().build();
    }
}
