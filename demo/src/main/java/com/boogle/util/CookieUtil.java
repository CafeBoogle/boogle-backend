package com.boogle.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); //https일경우엔 true로 해야함
        cookie.setPath("/");
        cookie.setDomain("moonsunpower.com");
        cookie.setMaxAge(60 * 15); // 15분
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
    }

    // Refresh Token
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS면 true
        cookie.setPath("/");
        cookie.setDomain("moonsunpower.com");
        cookie.setMaxAge(60 * 60 * 24 * 7); // 7일
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, null);
        cookie.setPath("/");
        cookie.setDomain("moonsunpower.com"); // 👈 생성할 때와 동일한 도메인 추가!
        cookie.setHttpOnly(true);             // 생성 시 설정했다면 삭제 시에도 맞추는 게 안전함
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        cookie.setMaxAge(0);                  // 즉시 만료
        response.addCookie(cookie);
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
        cookie.setPath("/");
        cookie.setDomain("moonsunpower.com"); // 👈 생성할 때와 동일한 도메인 추가!
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        cookie.setMaxAge(0);                  // 즉시 만료
        response.addCookie(cookie);
    }
}
