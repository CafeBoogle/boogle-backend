package com.boogle.controller;

import com.boogle.dto.NicknameRequestDto;
import com.boogle.entity.User;
import com.boogle.repository.UserRepository;
import com.boogle.service.UserService;
import com.boogle.util.CookieUtil;
import com.boogle.util.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    @PostMapping("/user/setup-nickname")
    public ResponseEntity<?> setupNicname(@Valid @RequestBody NicknameRequestDto dto,
                                          HttpServletResponse response) {
        User user = userService.updateNickname(dto.getUserId(), dto.getNickname());

        // 임시토큰이 아닌 정식 토큰으로 재발급
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getNickname(), user.getRole());

        // 기존 임시 쿠키를 덮어쓰거나 새로 추가
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        // 응답
        return ResponseEntity.ok().body("회원가입이 완료되었습니다.");

    }

}
