package com.boogle.controller;

import com.boogle.dto.CafeSaveRequestDto;
import com.boogle.service.CafeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CafeController {
    private final CafeService cafeService;

    @PostMapping("/cafes")
    public ResponseEntity<Long> checkAndSaveCafe(@Valid @RequestBody CafeSaveRequestDto dto) {
        // Db확인 및 저장 후 우리측 DB에서 ID(고유식별자)를 받음
        Long saveCafeId = cafeService.getOrCreateCafe(dto);

        // 프론트로  반환
        return ResponseEntity.ok(saveCafeId);
    }
}
