package entity;

import entity.type.Provider;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "USER_INFO",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_provider_provider_id",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    @Column(length = 50)
    private String nickname;

    @Column(name = "profile_image_name", nullable = false, length = 100)
    private String profileImageName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
