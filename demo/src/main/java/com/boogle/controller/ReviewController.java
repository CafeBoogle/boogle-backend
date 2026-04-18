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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<?> createReview(
            @RequestPart(value = "data")
            @Parameter(description = "리뷰 요청 데이터 (JSON 형식)", required = true,
                    schema = @Schema(implementation = ReviewRequest.class))
            ReviewRequest request,

            @RequestPart(value = "image", required = false)
            @Parameter(description = "리뷰 첨부 이미지, 필수는 아님")
            MultipartFile image
    ) {
        try {
            // TODO: 실제 프로젝트의 인증 로직에 따라 userId를 가져와야 합니다.
            Long userId = 1L;

            Long reviewId = reviewService.saveReview(request, image, userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(reviewId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 등록 중 오류가 발생했습니다.");
        }
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
}