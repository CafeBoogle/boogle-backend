package com.boogle.controller;

import com.boogle.component.NaverProperties;
import com.boogle.service.NaverService;
import com.boogle.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;

@Tag(name = "01-1. Naver Auth", description = "네이버 계정을 이용한 로그인 및 로그아웃")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NaverOAuthController {

    private final NaverService naverService;
    private final NaverProperties naverProperties;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend-url:/}")
    private String frontendUrl;

    @Operation(summary = "네이버 로그인 시작",
            description = "사용자를 네이버 아이디 로그인 페이지로 리다이렉트")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "네이버 로그인 페이지로 리다이렉트")
    })
    @GetMapping("/oauth/naver")
    public void naverLogin(
            @RequestParam(required = false) String redirect,
            HttpServletResponse response
    ) throws IOException {

        String encodedState = Base64.getEncoder().encodeToString(
                (redirect != null ? redirect : frontendUrl).getBytes()
        );

        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + naverProperties.getClientId()
                + "&redirect_uri=" + naverProperties.getRedirectUri()
                + "&state=" + encodedState;
        System.out.println("REAL redirect_uri = " + naverProperties.getRedirectUri());
        response.sendRedirect(naverAuthUrl);
    }

    @Operation(summary = "네이버 로그인 콜백",
            description = "네이버 인증 성공 후 받은 인가 코드로 access_token을 요청하고 로그인 처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 및 JWT 쿠키 발급 완료"),
            @ApiResponse(responseCode = "400", description = "인가 코드 누락 또는 위조된 요청(state 불일치)")
    })
    @GetMapping("/oauth/naver/callback")
    public void naverCallback(
            @Parameter(description = "네이버에서 발급한 인가 코드", required = true)
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String redirectUrl = new String(Base64.getDecoder().decode(state));

        naverService.login(code, redirectUrl, response);
    }

    @Operation(summary = "네이버 로그아웃",
            description = "서버에 저장된 인증 쿠키를 삭제하고 메인 페이지로 이동")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "쿠키 삭제 후 프론트엔드 URL로 리다이렉트")
    })
    @GetMapping("/oauth/naver/logout")
    public void naverLogout(
            @RequestParam(required = false) String redirect,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);

        String redirectUrl = redirect;

        // redirect 파라미터가 없으면 Origin 헤더 사용
        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = request.getHeader("Origin");
        }

        // Origin도 없으면 기본 frontendUrl 사용
        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = frontendUrl;
        }

        response.sendRedirect(frontendUrl);
    }
}