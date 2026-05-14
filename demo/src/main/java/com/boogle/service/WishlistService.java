package com.boogle.service;

import com.boogle.dto.CafeResponseDto;
import com.boogle.dto.CafeScoreResopnseDto;
import com.boogle.dto.CafeWithTagDto;
import com.boogle.dto.projection.CafeScoreProjection;
import com.boogle.entity.Cafe;
import com.boogle.entity.User;
import com.boogle.entity.Wishlist;
import com.boogle.repository.CafeRepository;
import com.boogle.repository.ReviewRepository;
import com.boogle.repository.UserRepository;
import com.boogle.repository.WishlistRepository;
import com.boogle.util.CafeTagGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CafeRepository cafeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CafeTagGenerator cafeTagGenerator;

    // 찜하기 로직
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

    // 카페 상세 진입 시 찜 여부 조회
    @Transactional(readOnly = true)
    public boolean isCafeWithed(Long userId, Long cafeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페를 찾을 수 없습니다."));

        return wishlistRepository.findByUserAndCafe(user, cafe).isPresent();
    }

    @Transactional(readOnly = true)
    public Long getWishlistCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return wishlistRepository.countByUser(user);
    }

    // 유저가 찜한 카페 보기 (태그 포함)
    @Transactional(readOnly = true)
    public List<CafeWithTagDto> getMyWishlist(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ✅ 1. 찜한 카페 조회
        List<Cafe> cafes = wishlistRepository.findCafesByUser(user);

        if (cafes.isEmpty()) {
            return List.of();
        }

        // ✅ 2. 카페 ID 추출
        List<Long> cafeIds = cafes.stream()
                .map(Cafe::getId)
                .toList();

        // ✅ 3. 카페 점수 조회
        List<CafeScoreProjection> projections =
                reviewRepository.findCafeScoresByCafeIds(cafeIds);

        Map<Long, CafeScoreResopnseDto> scoreMap = new HashMap<>();

        for (CafeScoreProjection p : projections) {
            scoreMap.put(
                    p.cafeId(),
                    CafeScoreResopnseDto.builder()
                            .cafeId(p.cafeId())
                            .reviewCount(p.reviewCount().intValue())
                            .toiletScoreAvg(p.toiletScoreAvg())
                            .outletScoreAvg(p.outletScoreAvg())
                            .seatScoreAvg(p.seatScoreAvg())
                            .wifiScoreAvg(p.wifiScoreAvg())
                            .noiseScoreAvg(p.noiseScoreAvg())
                            .studyScoreAvg(p.studyScoreAvg())
                            .build()
            );
        }

        // ✅ ✅ 4. 여기 추가 (핵심🔥)
        return cafes.stream()
                .map(cafe -> {

                    CafeScoreResopnseDto score = scoreMap.get(cafe.getId());

                    List<String> tags = cafeTagGenerator.generateTags(score);

                    // ✅ 태그 없을 때 대비
                    if (tags == null || tags.isEmpty()) {
                        tags = List.of("리뷰 부족");
                    }

                    return new CafeWithTagDto(
                            cafe.getId(),
                            cafe.getName(),
                            cafe.getAddress(),
                            tags
                    );
                })
                .toList();
    }
}