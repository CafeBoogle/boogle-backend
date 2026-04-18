package com.boogle.controller;

import com.boogle.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.boogle.service.KakaoService;

import java.io.IOException;

@Tag(name = "01-2. Kakao Auth", description = "카카오 계정을 이용한 로그인 및 로그아웃")
@RestController
@RequiredArgsConstructor
public class KakaoOAuthController {
    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.logout-redirect-uri}")
    private String logoutRedirectUri;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final CookieUtil cookieUtil;
    private final KakaoService kakaoService;


    @Operation(summary = "카카오 로그인 시작",
            description = "사용자를 카카오 인가 코드 요청 페이지로 리다이렉트")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "카카오 로그인 페이지로 이동")
    })
    @GetMapping("/api/oauth/kakao")public void kakaoLogin(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&prompt=login";

        response.sendRedirect(kakaoAuthUrl);
    }

    @Operation(summary = "카카오 로그인 콜백",
            description = "카카오 인증 성공 후 전달받은 인가 코드로 토큰을 발급받고 로그인을 완료")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 및 쿠키 세팅 완료"),
            @ApiResponse(responseCode = "400", description = "인가 코드 누락 또는 유효하지 않은 코드")
    })
    @GetMapping("/api/oauth/kakao/callback")
    public void kakaoCallback(@Parameter(description = "카카오에서 발급한 인가 코드", required = true) @RequestParam String code,
                              HttpServletResponse response) throws IOException {

        kakaoService.login(code, response);
    }

    @Operation(summary = "카카오 로그아웃",
            description = "인증 쿠키를 삭제하고, 카카오 계정 세션까지 종료하기 위해 카카오 로그아웃 페이지로 이동")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "카카오 로그아웃 페이지로 이동 및 쿠키 삭제")
    })
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

    @Operation(summary = "카카오 로그아웃 콜백",
            description = "카카오 로그아웃 처리가 완료된 후 프론트엔드 메인 페이지로 리다이렉트")
    @GetMapping("/api/oauth/kakao/logout/callback")
    public void kakaoLogoutCallback(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontendUrl + "/");
    }
}