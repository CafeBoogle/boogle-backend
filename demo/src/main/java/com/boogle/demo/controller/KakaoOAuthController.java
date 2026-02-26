package com.boogle.demo.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.boogle.demo.service.KakaoService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class KakaoOAuthController {

    private final KakaoService kakaoService;

    @GetMapping("/api/oauth/kakao/callback")
    public void kakaoCallback(@RequestParam String code,
                              HttpServletResponse response) throws IOException {

        kakaoService.login(code, response);
    }
    // 🚨 테스트용 API 추가
    @GetMapping("/api/oauth/test")
    public String securityTest() {
        System.out.println("✅ 시큐리티 통과 성공! 현관문이 열려있습니다.");
        return "Security is Open!";
    }
}