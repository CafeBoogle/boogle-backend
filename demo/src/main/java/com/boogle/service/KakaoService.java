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

    public void login(String code, String redirectUrl, HttpServletResponse response) throws IOException {
        String kakaoAccessToken = getKakaoAccessToken(code);
        Map<String, Object> body = getKakaoUserInfo(kakaoAccessToken);
        String providerUserId = String.valueOf(body.get("id"));

        // DB CHECK
        Optional<User> userOptional = userRepository.findByProviderAndProviderUserId(Provider.KAKAO, providerUserId);

        // EXISTING USER
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getNickname() == null) {
                response.sendRedirect(redirectUrl + "/signup?provider=KAKAO&userId=" + providerUserId);
                return;
            }

            String accessToken = jwtProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole());
            String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getNickname(), user.getRole());
            cookieUtil.addRefreshTokenCookie(response, refreshToken);
            response.sendRedirect(redirectUrl + "/?access_token=" + accessToken);
            return;
        }

        // NEW USER
        User newUser = User.builder()
                .provider(Provider.KAKAO)
                .providerUserId(providerUserId)
                .nickname(null)
                .profileImageName("default.png")
                .role(Role.USER)
                .build();

        userRepository.save(newUser);
        response.sendRedirect(redirectUrl + "/signup?provider=KAKAO&userId=" + providerUserId);
    }

    private String getKakaoAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Accept", "application/json");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("https://kauth.kakao.com/oauth/token", request, Map.class);
        return (String) response.getBody().get("access_token");
    }

    private Map getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, Map.class);
        return response.getBody();
    }
}