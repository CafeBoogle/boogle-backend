package com.boogle.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "REVIEWS")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    // 한줄리뷰
    @Column(length = 255)
    private String shortReview;

    // 이미지 리스트
    @OneToMany(
            mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    // 점수
    private Integer toiletScore;
    private Integer outletScore;
    private Integer seatScore;
    private Integer wifiScore;
    private Integer noiseScore;
    private Integer studyScore;

    // 시간
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(
            String shortReview,
            Integer toiletScore,
            Integer outletScore,
            Integer seatScore,
            Integer wifiScore,
            Integer noiseScore,
            Integer studyScore) {

        this.shortReview = shortReview;
        this.toiletScore = toiletScore;
        this.outletScore = outletScore;
        this.seatScore = seatScore;
        this.wifiScore = wifiScore;
        this.noiseScore = noiseScore;
        this.studyScore = studyScore;
    }
}
