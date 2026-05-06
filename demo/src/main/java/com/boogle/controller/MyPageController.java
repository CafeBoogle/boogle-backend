package com.boogle.controller;

import com.boogle.dto.MyReviewResponseDto;
import com.boogle.entity.User;
import com.boogle.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;


    @GetMapping("/reviews")
    public ResponseEntity<List<MyReviewResponseDto>> myReviews(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(myPageService.getMyReviews(userId));
    }
}
