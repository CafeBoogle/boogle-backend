package com.boogle.controller;

import com.boogle.dto.request.ReviewRequest;
import com.boogle.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "03. Review", description = "카페 리뷰 등록 및 관리 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "리뷰 등록 (이미지 포함)",
            description = "리뷰 정보(JSON)와 이미지 파일(MultipartFile)을 동시에 업로드 <br> " +
                    "**data** 파트에는 리뷰 내용(JSON)을, **image** 파트에는 사진 파일"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공: 생성된 리뷰의 고유 식별자(ID) 반환",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "실패: 필수 파라미터 누락 또는 잘못된 형식"),
            @ApiResponse(responseCode = "500", description = "실패: 서버 내부 오류 (파일 저장 실패 등)")
    })

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createReview(
            @RequestPart("data") ReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Long userId
    ) {

        if (request.getCafeId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "카페를 선택해야 리뷰를 등록할 수 있습니다."
            );
        }

        Long reviewId = reviewService.saveReview(request, images, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewId);
    }


    @Operation(summary = "리뷰 상세 조회", description = "리뷰의 고유 ID를 이용해 상세 내용을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 리뷰 상세 데이터 반환"),
            @ApiResponse(responseCode = "404", description = "실패: 존재하지 않는 리뷰 ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getReview(
            @Parameter(description = "리뷰 고유 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내가 남긴 리뷰 삭제", description = "리뷰의 고유 ID를 이용해 해당 리뷰 삭제")
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @AuthenticationPrincipal Long userId) {

        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

}