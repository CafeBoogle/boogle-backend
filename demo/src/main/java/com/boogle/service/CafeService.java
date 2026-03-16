package com.boogle.service;

import com.boogle.dto.CafeResponseDto;
import com.boogle.dto.CafeSaveRequestDto;
import com.boogle.entity.Cafe;
import com.boogle.repository.CafeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeService {
    private final CafeRepository cafeRepository;

    // 카카오맵 범위 내에 있는 카페를 목록화
    public List<CafeResponseDto> findCafesWithinBounds(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        List<Cafe> cafes = cafeRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);

        // Entity를 Dto로 변환
        return cafes.stream().map(cafe -> CafeResponseDto.builder()
                .id(cafe.getId())
                .name(cafe.getName())
                .address(cafe.getAddress())
                .latitude(cafe.getLatitude())
                .longitude(cafe.getLongitude())
                .thumbnail(cafe.getImageName())
                .build())
                .collect(Collectors.toList());

    }

    // 카페 목록에서 카페를 클릭 시 우리 DB에 저장하는 로직
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


