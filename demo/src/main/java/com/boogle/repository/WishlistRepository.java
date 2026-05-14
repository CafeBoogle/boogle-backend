package com.boogle.repository;

import com.boogle.entity.Cafe;
import com.boogle.entity.User;
import com.boogle.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // 특정 유저가 특정 카페를 '이미' 찜 했는지 확인
    Optional<Wishlist> findByUserAndCafe(User user, Cafe cafe);

    // 찜한 갯수 카운트
    Long countByUser(User user);

    // 유저가 찜한 카페 보기
    @Query("select w.cafe from Wishlist w where w.user = :user")
    List<Cafe> findCafesByUser(User user);

}
