package com.boogle.service;

import com.boogle.dto.request.ReviewRequest;
import com.boogle.entity.Cafe;
import com.boogle.entity.Review;
import com.boogle.entity.User;
import com.boogle.repository.CafeRepository;
import com.boogle.repository.ReviewRepository;
import com.boogle.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

        Review review = new Review();
        review.setUser(user);
        review.setCafe(cafe);
        review.setOutletScore(dto.getOutletScore());
        review.setSeatScore(dto.getSeatScore());
        review.setNoiseScore(dto.getNoiseScore());
        review.setToiletScore(dto.getToiletScore());
        review.setWifiScore(dto.getWifiScore());

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
}