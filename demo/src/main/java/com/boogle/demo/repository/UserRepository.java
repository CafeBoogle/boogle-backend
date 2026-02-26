package com.boogle.demo.repository;

import com.boogle.demo.entity.User;
import com.boogle.demo.entity.type.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderUserId(
            Provider provider,
            String providerUserId
    );

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}