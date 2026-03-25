package com.boogle.service;

import com.boogle.entity.User;
import com.boogle.entity.type.Provider;
import com.boogle.entity.type.Role;
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
import java.util.Optional;

@Service

@RequiredArgsConstructor
public class KakaoService {
    @Value("${app.frontend-url}")
    private String frontendUrl;
    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;
    @Value("${kakao.client-secret}")
    private String clientSecret;


    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    public void login(String code, HttpServletResponse response) throws IOException {

        // 1️⃣ 카카오 access token 발급
        String kakaoAccessToken = getKakaoAccessToken(code);

        // 2️⃣ 카카오 사용자 정보 조회
        // 2️⃣ 사용자 정보 조회
        Map body = getKakaoUserInfo(kakaoAccessToken);

        String providerUserId = String.valueOf(body.get("id"));

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) body.get("kakao_account");

        // 3️⃣ DB 조회
        Optional<User> optionalUser =
                userRepository.findByProviderAndProviderUserId(
                        Provider.KAKAO,
                        providerUserId
                );

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            if (user.getNickname() == null) {
                issueTempToken(user, response);
                response.sendRedirect(frontendUrl + "/signup");
            } else {
                issueFullToken(user, response);
                response.sendRedirect(frontendUrl + "/category");
            }

        } else {
            User newUser = User.builder()
                    .provider(Provider.KAKAO)
                    .providerUserId(providerUserId)
                    .nickname(null)
                    .role(Role.USER)
                    .profileImageName("default.png")
                    .build();

            userRepository.save(newUser);

            issueTempToken(newUser, response);
            System.out.println("providerUserId = " + providerUserId);
            System.out.println("optionalUser = " + optionalUser.isPresent());
            System.out.println("clientId="+clientId);
            System.out.println("redirectUri"+redirectUri);
            response.sendRedirect(frontendUrl + "/signup");
        }
    }
    private String getKakaoAccessToken(String code) {
        System.out.println("보낼 인가 코드: " + code);
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_secret", clientSecret);
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token",
                request,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    private Map getKakaoUserInfo(String accessToken) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                Map.class
        );

        return response.getBody();
    }

    private void issueTempToken(User user, HttpServletResponse response) {

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                null,                  // 닉네임 아직 없음
                user.getRole()
        );

        cookieUtil.addAccessTokenCookie(response, accessToken);
    }
    private void issueFullToken(User user, HttpServletResponse response) {

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getNickname(),
                user.getRole()
        );

        String refreshToken = jwtProvider.createRefreshToken(
                user.getId(),
                user.getNickname(),
                user.getRole()
        );

        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);
    }
}