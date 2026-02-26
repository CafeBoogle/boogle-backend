package com.boogle.demo.service;

import com.boogle.demo.entity.User;
import com.boogle.demo.entity.type.Provider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.boogle.demo.repository.UserRepository;
import com.boogle.demo.util.CookieUtil;
import com.boogle.demo.util.JwtProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;
    public void login(String code, HttpServletResponse response) throws IOException {

        System.out.println("✅ [1] 프론트에서 백엔드로 코드 무사히 도착! 인가 코드: " + code);

        RestTemplate restTemplate = new RestTemplate();

        // 1️⃣ 인가코드 → access_token 요청
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", clientId);
        tokenParams.add("redirect_uri", redirectUri);
        tokenParams.add("code", code);

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);

        try {
            System.out.println("✅ [2] 카카오로 토큰 발급 요청 중...");
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token", tokenRequest, Map.class
            );
            System.out.println("✅ [3] 카카오 토큰 발급 성공!");

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 2️⃣ access_token → 사용자 정보 요청
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);

            HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);
            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, userRequest, Map.class
            );

            Map body = userResponse.getBody();
            Long kakaoId = ((Number) body.get("id")).longValue();
            String providerUserId = String.valueOf(kakaoId);

            Map kakaoAccount = (Map) body.get("kakao_account");
            Map profile = kakaoAccount != null ? (Map) kakaoAccount.get("profile") : null;
            String nickname = profile != null ? (String) profile.get("nickname") : "kakao_user";

            // 3️⃣ DB 조회 및 신규 가입 여부 판단
            boolean isNewUser = false;
            Optional<User> optionalUser = userRepository.findByProviderAndProviderUserId(Provider.KAKAO, providerUserId);

            User user;
            if (optionalUser.isPresent()) {
                user = optionalUser.get(); // 기존 회원
            } else {
                isNewUser = true; // 신규 회원
                User newUser = new User();
                newUser.setProvider(Provider.KAKAO);
                newUser.setProviderUserId(providerUserId);
                newUser.setNickname(nickname);
                newUser.setProfileImageName("default.png");
                user = userRepository.save(newUser);
            }

            // 4️⃣ JWT 발급
            String jwt = jwtProvider.createdAccessToken(user.getId());
            cookieUtil.addAccessTokenCookie(response, jwt);

            // 5️⃣ 프론트로 리다이렉트 (5173 포트로 변경)
            String frontendRedirectUrl = "http://localhost:5173/auth/success";

            System.out.println("✅ [4] 모든 처리 완료! 프론트로 리다이렉트 합니다.");
            if (isNewUser) {
                response.sendRedirect(frontendRedirectUrl + "?isNewUser=true");
            } else {
                response.sendRedirect(frontendRedirectUrl + "?isNewUser=false");
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("🚨 카카오 API 에러 터짐!!! 🚨");
            System.err.println("에러 코드: " + e.getStatusCode());
            System.err.println("상세 내용: " + e.getResponseBodyAsString());
            // 강제로 에러 페이지 띄우기 (콘솔 확인용)
            throw new RuntimeException("카카오 인증 실패");
        } catch (Exception e) {
            System.err.println("🚨 서버 내부 에러 터짐!!! 🚨");
            e.printStackTrace();
            throw new RuntimeException("서버 에러");
        }
    }
}