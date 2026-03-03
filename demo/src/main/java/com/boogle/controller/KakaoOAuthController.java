package com.boogle.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.boogle.service.KakaoService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class KakaoOAuthController {
    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final KakaoService kakaoService;
    @GetMapping("/api/oauth/kakao")
    public void kakaoLogin(HttpServletResponse response) throws IOException {

        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;

        response.sendRedirect(kakaoAuthUrl);
    }

    @GetMapping("/api/oauth/kakao/callback")
    public void kakaoCallback(@RequestParam String code,
                              HttpServletResponse response) throws IOException {

        kakaoService.login(code, response);
    }
}