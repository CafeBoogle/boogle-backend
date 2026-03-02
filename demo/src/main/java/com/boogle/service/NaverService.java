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
public class NaverService {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}") // 1. 시크릿 키 필드 추가
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    public void login(String code, HttpServletResponse response) throws IOException {

        RestTemplate restTemplate = new RestTemplate();

        // 인가코드 -> access_token 요청
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", clientId);
        tokenParams.add("client_secret", clientSecret); // 2. 요청 파라미터에 시크릿 추가
        tokenParams.add("redirect_uri", redirectUri);
        tokenParams.add("code", code);
        // state 검증이 필요하다면 여기에 state도 추가할 수 있습니다.

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(tokenParams, tokenHeaders);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://nid.naver.com/oauth2.0/token",
                tokenRequest,
                Map.class
        );

        String naverAccessToken  = (String) tokenResponse.getBody().get("access_token");

        // access_token -> 사용자 정보 요청
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(naverAccessToken );

        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map body = userResponse.getBody();
        Map responseMap = (Map) body.get("response"); // 네이버는 실제 정보가 "response" 안에 있음

        String providerUserId = (String) responseMap.get("id"); // 네이버 ID는 보통 String으로 옵니다.
        String nickname = (String) responseMap.get("nickname");

        Map naverAccount = (Map) body.get("naver_account");
        Map profile = naverAccount != null ? (Map) naverAccount.get("profile") : null;

        // DB 조회 또는 회원가입
        User user = userRepository
                .findByProviderAndProviderUserId(Provider.NAVER, providerUserId)
                .orElseGet(() -> {

                    User newUser = new User();
                    newUser.setProvider(Provider.NAVER);
                    newUser.setProviderUserId(providerUserId);
                    newUser.setNickname(nickname);
                    newUser.setProfileImageName("default.png");

                    return userRepository.save(newUser);
                });

        // JWT 발급
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId()); // refreshToken도 같이 발급

        // 쿠키 저장
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        // 프론트로 리다이렉트
        response.sendRedirect("http://localhost:3000");
    }
}