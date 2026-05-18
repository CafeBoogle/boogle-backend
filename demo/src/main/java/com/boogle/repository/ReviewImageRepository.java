package com.boogle.repository;

import com.boogle.entity.ReviewImage;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    // 리뷰 이미지 삭제
    @Modifying
    @Query("delete from ReviewImage ri where ri.review.id = :reviewId")
    void deleteByReview_Id(Long reviewId);

    // 특정 이미지만 삭제
    void deleteAllByIdIn(List<Long> ids);
}
