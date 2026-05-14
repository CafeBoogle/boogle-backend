package com.boogle.controller;

import com.boogle.dto.MyReviewResponseDto;
import com.boogle.entity.User;
import com.boogle.service.MyPageService;
import com.boogle.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
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
    private final WishlistService wishlistService;

    @Operation(summary = "내가 남긴 리뷰 조회")
    @GetMapping("/reviews")
    public ResponseEntity<List<MyReviewResponseDto>> myReviews(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(myPageService.getMyReviews(userId));
    }

    @Operation(summary = "내 찜한 카페 개수 조회")
    @GetMapping("/wish/count")
    public ResponseEntity<Long> getWishlistCount(@AuthenticationPrincipal Long userId) {
        if(userId == null) {
            return ResponseEntity.status(401).build();
        }

        Long count = wishlistService.getWishlistCount(userId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "찜한 카페 보기")
    @GetMapping("/wish")
    public ResponseEntity<?> getMyWishlist(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(wishlistService.getMyWishlist(userId));
    }
}
