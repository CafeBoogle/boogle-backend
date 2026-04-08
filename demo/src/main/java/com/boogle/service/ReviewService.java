package com.boogle.service;

import com.boogle.dto.CafeScoreResopnseDto;
import com.boogle.dto.projection.CafeScoreProjection;
import com.boogle.dto.request.ReviewRequest;
import com.boogle.entity.Cafe;
import com.boogle.entity.Review;
import com.boogle.entity.User;
import com.boogle.repository.CafeRepository;
import com.boogle.repository.ReviewRepository;
import com.boogle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;

    // 로컬 저장 경로 (실제 경로로 수정 필요)
    private final String uploadPath = "C:/uploads/reviews/";

    @Transactional
    public Long saveReview(ReviewRequest dto, MultipartFile image, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Cafe cafe = cafeRepository.findById(dto.getCafeId())
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        // 카페 id기준 리뷰 저장
        Review review = Review.builder()
                .user(user)
                .cafe(cafe)
                .shortReview(dto.getShortReview())
                .imageName(dto.getImageName())
                .toiletScore(dto.getToiletScore())
                .outletScore(dto.getOutletScore())
                .seatScore(dto.getSeatScore())
                .wifiScore(dto.getWifiScore())
                .noiseScore(dto.getNoiseScore())
                .build();

        // 이미지 파일 처리
        if (image != null && !image.isEmpty()) {
            String originalName = image.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String savedName = uuid + "_" + originalName;

            try {
                File saveFile = new File(uploadPath + savedName);
                image.transferTo(saveFile);
                review.setImageName(savedName); // DB에는 저장된 파일명 기록
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }

        return reviewRepository.save(review).getId();
    }

    // 리뷰가 있을 때 평균내기
    @Transactional(readOnly = true)
    public CafeScoreResopnseDto getCafeScore(Long cafeId) {

        CafeScoreProjection p = reviewRepository.findCafeScoreAverages(cafeId);

        return CafeScoreResopnseDto.builder()
                .cafeId(cafeId)
                .toiletScoreAvg(p.toiletScoreAvg())
                .outletScoreAvg(p.outletScoreAvg())
                .seatScoreAvg(p.seatScoreAvg())
                .wifiScoreAvg(p.wifiScoreAvg())
                .noiseScoreAvg(p.noiseScoreAvg())
                .reviewCount(p.reviewCount().intValue())
                .build();
    }

    // 리뷰가 없을 때
    private Double defaultZero(Double value) {
        return value == null ? 0.0 : value;
    }
}