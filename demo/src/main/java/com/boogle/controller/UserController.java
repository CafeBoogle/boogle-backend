package com.boogle.controller;

import com.boogle.dto.NicknameRequestDto;
import com.boogle.dto.request.SignUpRequest;
import com.boogle.entity.User;
import com.boogle.repository.UserRepository;
import com.boogle.service.UserService;
import com.boogle.util.CookieUtil;
import com.boogle.util.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01. User & Auth", description = "회원가입, 로그아웃, 마이페이지 및 인증 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    @PostMapping("/user/setup-nickname")
    public ResponseEntity<?> setupNickname(
            @Valid @RequestBody NicknameRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            HttpServletResponse response) {

        User user = userService.updateNickname(userId, dto.getNickname());

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getNickname(), user.getRole());

        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        return ResponseEntity.ok().body("회원가입이 완료되었습니다.");
    }

    @PostMapping("/user/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok().body("로그아웃 성공");
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<?> signUp(@ModelAttribute SignUpRequest request, HttpServletResponse response) {
        User user = userService.signUp(request);

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getNickname(), user.getRole());

        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        return ResponseEntity.ok().body("회원가입 및 로그인 완료");
    }
}