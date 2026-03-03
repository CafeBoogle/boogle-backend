package com.boogle.repository;

import com.boogle.entity.User;
import com.boogle.entity.type.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderUserId(
            Provider provider,
            String providerUserId
    );

    Boolean existsByNickname(String nickname);
}