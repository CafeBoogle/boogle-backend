package com.boogle.controller;

import com.boogle.dto.CafeResponseDto;
import com.boogle.dto.CafeSaveRequestDto;
import com.boogle.service.CafeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CafeController {
    private final CafeService cafeService;

    // 카페 목록에서 카페 클릭 시 우리 DB로 저장하는 메서드
    @PostMapping("/cafes")
    public ResponseEntity<Long> checkAndSaveCafe(@Valid @RequestBody CafeSaveRequestDto dto) {
        // Db확인 및 저장 후 우리측 DB에서 ID(고유식별자)를 받음
        Long saveCafeId = cafeService.getOrCreateCafe(dto);

        // 프론트로  반환
        return ResponseEntity.ok(saveCafeId);
    }

    // 카카오 지도 범위 내 카페를 목록화 하는 메서드
    @GetMapping("/cafes/within_bounds")
    public ResponseEntity<List<CafeResponseDto>> getCafesWithinBounds(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng
    ) {
        List<CafeResponseDto> cafes = cafeService.findCafesWithinBounds(minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(cafes);
    }
}
