package com.boogle.service;

import com.boogle.entity.User;
import com.boogle.entity.type.Provider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.boogle.repository.UserRepository;
import com.boogle.util.CookieUtil;
import com.boogle.util.JwtProvider;

import java.io.IOException;
import java.util.Map;

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

        RestTemplate restTemplate = new RestTemplate();

        // 1️⃣ 인가코드 → access_token 요청
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", clientId);
        tokenParams.add("redirect_uri", redirectUri);
        tokenParams.add("code", code);

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(tokenParams, tokenHeaders);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                tokenRequest,
                Map.class
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2️⃣ access_token → 사용자 정보 요청
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map body = userResponse.getBody();
        Long kakaoId = ((Number) body.get("id")).longValue();
        String providerUserId = String.valueOf(kakaoId);

        Map kakaoAccount = (Map) body.get("kakao_account");
        Map profile = kakaoAccount != null ? (Map) kakaoAccount.get("profile") : null;
        String nickname = profile != null ? (String) profile.get("nickname") : "kakao_user";

        // 3️⃣ DB 조회 또는 회원가입
        User user = userRepository
                .findByProviderAndProviderUserId(Provider.KAKAO, providerUserId)
                .orElseGet(() -> {

                    User newUser = new User();
                    newUser.setProvider(Provider.KAKAO);
                    newUser.setProviderUserId(providerUserId);
                    newUser.setNickname(nickname);
                    newUser.setProfileImageName("default.png");

                    return userRepository.save(newUser);
                });

        // 4️⃣ JWT 발급
        String jwt = jwtProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole());

        // 5️⃣ 쿠키 저장
        cookieUtil.addAccessTokenCookie(response, jwt);

        // 6️⃣ 프론트로 리다이렉트
        response.sendRedirect("http://localhost:3000");
    }
}