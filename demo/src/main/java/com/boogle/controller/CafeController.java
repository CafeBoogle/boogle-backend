package com.boogle.controller;

import com.boogle.dto.CafeDetailResponseDto;
import com.boogle.dto.CafeResponseDto;
import com.boogle.dto.CafeSaveRequestDto;
import com.boogle.dto.CafeScoreResopnseDto;
import com.boogle.dto.request.ReviewRequest;
import com.boogle.entity.Review;
import com.boogle.repository.WishlistRepository;
import com.boogle.service.CafeService;
import com.boogle.service.ReviewService;
import com.boogle.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cafes")
public class CafeController {
    private final CafeService cafeService;
    private final WishlistService wishlistService;
    private final ReviewService reviewService;

    // 카페 목록에서 카페 클릭 시 우리 DB로 저장
    @PostMapping("/save")
    public ResponseEntity<Long> checkAndSaveCafe(@Valid @RequestBody CafeSaveRequestDto dto) {
        // Db확인 및 저장 후 우리측 DB에서 ID(고유식별자)를 받음
        Long saveCafeId = cafeService.getOrCreateCafe(dto);

        // 프론트로  반환
        return ResponseEntity.ok(saveCafeId);
    }
    // 카카오 지도 범위 내 카페를 목록화
    @GetMapping("/within_bounds")
    public ResponseEntity<List<CafeResponseDto>> getCafesWithinBounds(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng
    ) {
        List<CafeResponseDto> cafes = cafeService.findCafesWithinBounds(minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(cafes);
    }

    // 카페 찜하기
    @PostMapping("/{cafeId}/wish")
    public ResponseEntity<Boolean> toggleWishlist(
            @PathVariable Long cafeId,
            @AuthenticationPrincipal Long userId) { // User 대신 Long userId로 변경

        if (userId == null) {
            return ResponseEntity.status(401).build(); // 인증 실패 시 처리
        }

        boolean isWish = wishlistService.toggleWishlistById(userId, cafeId);
        return ResponseEntity.ok(isWish);
    }

    // 카페 상세 + 그래프 점수 조회
    @GetMapping("/{cafeId}")
    public ResponseEntity<CafeDetailResponseDto> getCafeDetail(@PathVariable Long cafeId) {
        return ResponseEntity.ok(cafeService.getCafeDetail(cafeId));
    }
}