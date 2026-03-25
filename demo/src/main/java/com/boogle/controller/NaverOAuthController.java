package com.boogle.controller;

import com.boogle.component.NaverProperties;
import com.boogle.dto.NicknameRequestDto;
import com.boogle.entity.User;
import com.boogle.repository.UserRepository;
import com.boogle.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.boogle.service.NaverService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NaverOAuthController {

    private final NaverService naverService;
    private final NaverProperties naverProperties;
    private final CookieUtil cookieUtil;

    @GetMapping("/oauth/naver/callback")
    public void naverCallback(@RequestParam String code,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {

        naverService.login(code, response);
    }

    @GetMapping("/oauth/naver")
    public void naverLogin(HttpServletResponse response) throws IOException {
        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + naverProperties.getClientId()
                + "&redirect_uri=" + naverProperties.getRedirectUri()
                + "&state=random";

        response.sendRedirect(naverAuthUrl);
    }

    @GetMapping("/oauth/naver/logout")
    public void kakaoLogout(HttpServletResponse response) throws IOException {
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);
        response.sendRedirect("http://localhost:5173/"); // 일단 로컬 프론트 경로로
    }
}