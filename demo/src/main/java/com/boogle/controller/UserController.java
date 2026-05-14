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

    @Operation(summary = "닉네임 설정 및 정식 토큰 발급",
            description = "소셜 로그인 후 최초 닉네임을 설정. 완료 시 기존 임시 토큰 대신 닉네임 정보가 포함된 정식 JWT 토큰이 쿠키에 저장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 닉네임 설정 및 토큰 갱신 완료"),
            @ApiResponse(responseCode = "400", description = "실패: 중복된 닉네임 혹은 유효하지 않은 형식"),
            @ApiResponse(responseCode = "401", description = "실패: 유효하지 않은 임시 토큰")
    })
    @PostMapping("/user/setup-nickname")
    public ResponseEntity<?> setupNickname(
            @Valid @RequestBody NicknameRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            HttpServletResponse response) {

        User user = userService.updateNickname(userId, dto.getNickname());

        // 정식 토큰 재발급
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getNickname(), user.getRole());

        cookieUtil.addRefreshTokenCookie(response, refreshToken);


        return ResponseEntity.ok().body(
                java.util.Map.of(
                        "message", "회원가입이 완료되었습니다.",
                        "accessToken", accessToken
                )
        );
    }

    @Operation(summary = "로그아웃", description = "브라우저에 저장된 Access Token 및 Refresh Token 쿠키를 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 로그아웃 완료")
    })
    @PostMapping("/user/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok().body("로그아웃 성공");
    }

    @Operation(summary = "내 정보 조회", description = "JWT 토큰을 기반으로 현재 로그인한 사용자의 프로필 정보 가져옴.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 사용자 정보 반환",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "실패: 토큰 만료 또는 인증 실패")
    })
    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "자체 회원가입", description = "아이디/비밀번호 방식의 자체 회원가입을 진행 (프로필 이미지 포함 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공: 회원가입 완료")
    })
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String signUp(@ModelAttribute SignUpRequest request) {
        userService.signUp(request);
        return "회원가입 완료!";
    }
}