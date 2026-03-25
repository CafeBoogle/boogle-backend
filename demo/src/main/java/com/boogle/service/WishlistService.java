package com.boogle.service;

import com.boogle.entity.Cafe;
import com.boogle.entity.User;
import com.boogle.entity.Wishlist;
import com.boogle.repository.CafeRepository;
import com.boogle.repository.UserRepository;
import com.boogle.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CafeRepository cafeRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleWishlistById(Long userId, Long cafeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        Optional<Wishlist> existingWishlist = wishlistRepository.findByUserAndCafe(user, cafe);

        if (existingWishlist.isPresent()) {
            wishlistRepository.delete(existingWishlist.get());
            return false; // 찜 해제됨
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .cafe(cafe)
                    .build();
            wishlistRepository.save(wishlist);
            return true; // 찜 등록됨
        }
    }
}
