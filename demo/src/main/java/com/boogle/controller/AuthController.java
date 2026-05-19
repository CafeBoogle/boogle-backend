package com.boogle.controller;

import com.boogle.entity.User;
import com.boogle.entity.type.Role;
import com.boogle.repository.UserRepository;
import com.boogle.util.CookieUtil;
import com.boogle.util.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;
    private UserRepository userRepository;

    @Operation(summary = "accessToken 만료 시 refreshToken 으로 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (CookieUtil.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh token 없음");
        }

        try {
            if (!jwtProvider.validateToken(refreshToken)) {
                return ResponseEntity.status(401).body("Refresh token 만료");
            }

            Long userId = jwtProvider.getUserId(refreshToken);
            String nickname = jwtProvider.getNickname(refreshToken);

            Optional<User> user = userRepository.findById(userId);
            Role role = user.get().getRole();

            String newAccessToken = jwtProvider.createAccessToken(userId, nickname, role);

            // ✅ 쿠키 대신 바디로 반환
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("토큰 오류");
        }
    }
}

