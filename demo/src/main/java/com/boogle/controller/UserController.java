package com.boogle.controller;

import com.boogle.dto.NicknameRequestDto;
import com.boogle.dto.request.SignUpRequest;
import com.boogle.entity.User;
import com.boogle.repository.UserRepository;
import com.boogle.service.UserService;
import com.boogle.util.CookieUtil;
import com.boogle.util.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    @PostMapping("/user/setup-nickname")
    public ResponseEntity<?> setupNickname(@Valid @RequestBody NicknameRequestDto dto,
                                           @AuthenticationPrincipal Long userId,
                                           HttpServletResponse response) {

        // dto.getUserId() 대신 인증 정보에서 가져온 userId 사용
        User user = userService.updateNickname(userId, dto.getNickname());

        // 임시토큰이 아닌 정식 토큰으로 재발급
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getNickname(), user.getRole());

        // 기존 임시 쿠키를 덮어쓰거나 새로 추가
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        // 응답
        return ResponseEntity.ok().body("회원가입이 완료되었습니다.");

    }
    @PostMapping("/user/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // CookieUtil을 사용하여 쿠키 삭제
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);

        return ResponseEntity.ok().body("로그아웃 성공");
    }

    // UserController.java
//    @GetMapping("/user/me")
//    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Long userId) {
//        // SecurityContextHolder에서 인증된 userId를 가져옴
//        return userRepository.findById(userId)
//                .map(user -> ResponseEntity.ok(user)) // 프론트의 User interface와 구조 맞춰서 반환
//                .orElse(ResponseEntity.status(401).build());
//    }


    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Long userId) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String signUp(@ModelAttribute SignUpRequest request) {
        userService.signUp(request);
        return "회원가입 완료!";
    }
}
