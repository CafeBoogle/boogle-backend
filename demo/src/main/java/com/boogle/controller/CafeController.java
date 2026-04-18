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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "02. Cafe", description = "카페 정보 저장, 조회 및 찜하기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cafes")
public class CafeController {
    private final CafeService cafeService;
    private final WishlistService wishlistService;
    private final ReviewService reviewService;

    @Operation(summary = "카페 정보 저장/확인", description = "카카오 지도에서 클릭한 카페 정보를 DB에 저장/정보 가져오기")@PostMapping("/save")
    public ResponseEntity<Long> checkAndSaveCafe(@Valid @RequestBody CafeSaveRequestDto dto) {
        // Db확인 및 저장 후 우리측 DB에서 ID(고유식별자)를 받음
        Long saveCafeId = cafeService.getOrCreateCafe(dto);

        // 프론트로  반환
        return ResponseEntity.ok(saveCafeId);
    }
    // 카카오 지도 범위 내 카페를 목록화
    @Operation(summary = "지도 범위 내 카페 목록 조회", description = "좌표범위를 입력받아 해당 영역의 카페 리스트를 반환")
    @GetMapping("/within_bounds")
    public ResponseEntity<List<CafeResponseDto>> getCafesWithinBounds(@Parameter(description = "최소 위도", example = "37.123") @RequestParam Double minLat,
    @Parameter(description = "최대 위도", example = "37.456") @RequestParam Double maxLat,
    @Parameter(description = "최소 경도", example = "127.123") @RequestParam Double minLng,
    @Parameter(description = "최대 경도", example = "127.456") @RequestParam Double maxLng
    ) {
        List<CafeResponseDto> cafes = cafeService.findCafesWithinBounds(minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(cafes);
    }

    @Operation(summary = "카페 찜하기 토글", description = "카페를 찜 목록에 추가하거나 삭제")
    @PostMapping("/{cafeId}/wish")
    public ResponseEntity<Boolean> toggleWishlist(
            @Parameter(description = "카페 고유 ID", example = "1") @PathVariable Long cafeId,
            @AuthenticationPrincipal Long userId) { // User 대신 Long userId로 변경

        if (userId == null) {
            return ResponseEntity.status(401).build(); // 인증 실패 시 처리
        }

        boolean isWish = wishlistService.toggleWishlistById(userId, cafeId);
        return ResponseEntity.ok(isWish);
    }

    @Operation(summary = "카페 상세 정보 및 분석 점수 조회",
            description = "특정 카페의 상세 정보(이름, 주소 등)와 리뷰 기반의 그래프 점수 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 카페 상세 데이터 반환",
                    content = @Content(schema = @Schema(implementation = CafeDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "실패: 해당 ID의 카페를 찾을 수 없음")
    })
    @GetMapping("/{cafeId}")
    public ResponseEntity<CafeDetailResponseDto> getCafeDetail(@PathVariable Long cafeId) {
        return ResponseEntity.ok(cafeService.getCafeDetail(cafeId));
    }
}