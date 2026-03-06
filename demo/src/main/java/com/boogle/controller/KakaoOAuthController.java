package com.boogle.controller;

import com.boogle.util.CookieUtil;
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
    @Value("${kakao.logout-redirect-uri}")
    private String logoutRedirectUri;

    private final CookieUtil cookieUtil;
    private final KakaoService kakaoService;
    @GetMapping("/api/oauth/kakao")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&prompt=login";

        response.sendRedirect(kakaoAuthUrl);
    }

    @GetMapping("/api/oauth/kakao/callback")
    public void kakaoCallback(@RequestParam String code,
                              HttpServletResponse response) throws IOException {

        kakaoService.login(code, response);
    }

    @GetMapping("/api/oauth/kakao/logout")
    public void kakaoLogout(HttpServletResponse response) throws IOException {
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);

        // 2. 카카오 계정 세션까지 종료시키는 URL 생성
        String kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout"
                + "?client_id=" + clientId
                + "&logout_redirect_uri=" + logoutRedirectUri;

        // 3. 카카오 로그아웃 페이지로 이동
        response.sendRedirect(kakaoLogoutUrl);
    }
    // KakaoOAuthController.java

    @GetMapping("/api/oauth/kakao/logout/callback")
    public void kakaoLogoutCallback(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:5173/");
    }
}