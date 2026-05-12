package com.boogle.service;

import com.boogle.dto.CafeScoreResopnseDto;
import com.boogle.dto.projection.CafeScoreProjection;
import com.boogle.dto.request.ReviewRequest;
import com.boogle.entity.Cafe;
import com.boogle.entity.Review;
import com.boogle.entity.ReviewImage;
import com.boogle.entity.User;
import com.boogle.repository.CafeRepository;
import com.boogle.repository.ReviewRepository;
import com.boogle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;

    @Value("${file.upload-dir.review}")
    private String uploadPath;

    @Transactional
    public Long saveReview(ReviewRequest dto, List<MultipartFile> images, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Cafe cafe = cafeRepository.findById(dto.getCafeId())
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        // 카페 id기준 리뷰 저장
        Review review = Review.builder()
                .user(user)
                .cafe(cafe)
                .shortReview(dto.getShortReview())
                .toiletScore(dto.getToiletScore())
                .outletScore(dto.getOutletScore())
                .seatScore(dto.getSeatScore())
                .wifiScore(dto.getWifiScore())
                .noiseScore(dto.getNoiseScore())
                .studyScore(dto.getStudyScore())
                .build();

        // 이미지 파일 처리
        if (images != null && !images.isEmpty()) {
            int sortOrder = 0;

            File dir = new File(uploadPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("upload dir created: " + created);
            }

            for (MultipartFile image : images) {

                System.out.println("image name=" + image.getOriginalFilename());
                System.out.println("image size=" + image.getSize());

                if (image.isEmpty()) continue;

                String saveName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                File saveFile = new File(dir, saveName);

                System.out.println("saving image to: " + saveFile.getAbsolutePath());

                try {
                    image.transferTo(saveFile); // ✅ 이거 하나만
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("이미지 저장 실패", e);
                }

                ReviewImage reviewImage = ReviewImage.builder()
                        .review(review)
                        .imageUrl(saveName)
                        .sortOrder(sortOrder++)
                        .build();

                review.getImages().add(reviewImage);
            }
        }

        System.out.println("images is null? " + (images == null));
        System.out.println("images size = " + (images == null ? "null" : images.size()));

        try {
            return reviewRepository.save(review).getId();
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // 리뷰가 있을 때 평균내기
    @Transactional(readOnly = true)
    public CafeScoreResopnseDto getCafeScore(Long cafeId) {

        CafeScoreProjection p = reviewRepository.findCafeScoreByCafeId(cafeId);

        // 리뷰가 없는 경우
        if (p == null) {
            return CafeScoreResopnseDto.builder()
                    .cafeId(cafeId)
                    .toiletScoreAvg(0.0)
                    .outletScoreAvg(0.0)
                    .seatScoreAvg(0.0)
                    .wifiScoreAvg(0.0)
                    .noiseScoreAvg(0.0)
                    .studyScoreAvg(0.0)
                    .reviewCount(0)
                    .build();
        }

        return CafeScoreResopnseDto.builder()
                .cafeId(cafeId)
                .toiletScoreAvg(p.toiletScoreAvg())
                .outletScoreAvg(p.outletScoreAvg())
                .seatScoreAvg(p.seatScoreAvg())
                .wifiScoreAvg(p.wifiScoreAvg())
                .noiseScoreAvg(p.noiseScoreAvg())
                .studyScoreAvg(defaultZero(p.studyScoreAvg()))
                .reviewCount(p.reviewCount().intValue())
                .build();
    }

    // 리뷰가 없을 때
    private Double defaultZero(Double value) {
        return value == null ? 0.0 : value;
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 없거나 삭제 권한이 없습니다."));

        reviewRepository.delete(review);
    }

    // 리뷰 이미지 가져오기
    @Transactional(readOnly = true)
    public List<String> getPreviewReviewImages(Long cafeId) {
        return reviewRepository.findPreviewReviewImages(
                cafeId,
                PageRequest.of(0, 20) // 최근 20장
        );
    }

    // 한줄리뷰 가져오기

    @Transactional(readOnly = true)
    public List<String> getCafeShortReviews(Long cafeId) {
        return reviewRepository.findShortReviewsByCafeId(
                cafeId,
                PageRequest.of(0, 10)
        );
    }


}
