package com.boogle.repository;

import com.boogle.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 카페의 모든 리뷰 조회 (최신순)
    List<Review> findByCafeIdOrderByCreatedAtDesc(Long cafeId);

    // 특정 사용자가 작성한 리뷰 조회
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
}