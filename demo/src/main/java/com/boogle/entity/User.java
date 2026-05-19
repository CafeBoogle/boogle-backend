package com.boogle.entity;

import com.boogle.entity.type.Provider;
import com.boogle.entity.type.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    @Column(nullable = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "profile_image_name", nullable = false, length = 100)
    private String profileImageName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 권한(Role)을 시큐리티가 이해할 수 있는 형태로 반환
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getPassword() {
        // 소셜 로그인은 비밀번호가 없으므로 null 반환
        return null;
    }

    @Override
    public String getUsername() {
        // 사용자를 식별할 수 있는 고유값 (닉네임이나 providerUserId)
        return this.providerUserId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }
}