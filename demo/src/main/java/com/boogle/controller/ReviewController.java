package com.boogle.controller;
import com.boogle.dto.request.ReviewRequest;
import com.boogle.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 등록 API
     * @param request JSON 데이터 (ReviewRequest 클래스)
     * @param image 업로드할 사진 파일 (선택)
     * @return 생성된 리뷰의 ID
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createReview(
            @RequestPart("data") ReviewRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            // TODO: 실제 프로젝트의 인증 로직에 따라 userId를 가져와야 합니다.
            // 현재는 임시로 1L을 넘깁니다.
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

    /**
     * 리뷰 상세 조회 (필요 시 구현)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReview(@PathVariable Long id) {
        // 상세 조회 로직 구현
        return ResponseEntity.ok().build();
    }
}