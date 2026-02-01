package entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEWS",
        indexes = {
                @Index(name = "idx_review_cafe", columnList = "cafe_id"),
                @Index(name = "idx_review_user", columnList = "user_id")
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관계

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    // 리뷰 내용

    @Column(length = 255)
    private String shortReview;

    @Column(length = 100)
    private String imageName;

    // 육각형 차트

    @Column(nullable = false)
    private Integer toiletScore;

    @Column(nullable = false)
    private Integer outletScore;

    @Column(nullable = false)
    private Integer seatScore;

    @Column(nullable = false)
    private Integer wifiScore;

    @Column(nullable = false)
    private Integer noiseScore;

    // 시간

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 라이프사이클

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
