package com.boogle.service;

import com.boogle.dto.CafeSaveRequestDto;
import com.boogle.entity.Cafe;
import com.boogle.repository.CafeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CafeService {
    private final CafeRepository cafeRepository;

    @Transactional
    public long getOrCreateCafe(CafeSaveRequestDto dto) {
        // kakaoPlaceId로 DB를 조회
        // if (이미 존재할 경우) 객체를 그대로 반환
        // else DB에 저장 후 반환
        Cafe cafe = cafeRepository.findByKakaoPlaceId(dto.getKakaoPlaceId())
                .orElseGet(() -> cafeRepository.save(dto.toEntity()));

        // 우리 DB에 저장된 고유식별자(ID)를 프론트로 넘김
        return cafe.getId();
    }
}


