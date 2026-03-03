package com.boogle.repository;

import com.boogle.entity.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    Optional<Cafe> findByKakaoPlaceId(String kakaoPlaceId);
}
