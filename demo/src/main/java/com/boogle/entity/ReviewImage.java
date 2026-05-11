package com.boogle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "REVIEW_IMAGES",
        indexes = @Index(name = "idx_review_image_review", columnList = "review_id"))
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    // 이미지 정보
    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder; // 이미지 순서 (선택)
}