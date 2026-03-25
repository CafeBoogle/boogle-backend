package com.boogle.repository;

import com.boogle.entity.Cafe;
import com.boogle.entity.User;
import com.boogle.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // 특정 유저가 특정 카페를 '이미' 찜 했는지 확인
    Optional<Wishlist> findByUserAndCafe(User user, Cafe cafe);

    // 찜한 갯수 카운트
    Long countByCafe(Cafe cafe);
}
