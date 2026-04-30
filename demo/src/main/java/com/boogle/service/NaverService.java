package com.boogle.service;

import com.boogle.entity.User;
import com.boogle.entity.type.Provider;
import com.boogle.repository.UserRepository;
import com.boogle.util.CookieUtil;
import com.boogle.util.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NaverService {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    public void login(String code, String redirectUrl, HttpServletResponse response) throws IOException {

        RestTemplate restTemplate = new RestTemplate();

        // 1. TOKEN 요청
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", clientId);
        tokenParams.add("client_secret", clientSecret);
        tokenParams.add("redirect_uri", redirectUri);
        tokenParams.add("code", code);

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(tokenParams, tokenHeaders);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                "https://nid.naver.com/oauth2.0/token",
                tokenRequest,
                Map.class
        );

        String naverAccessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. USER INFO 요청
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(naverAccessToken);

        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map body = userResponse.getBody();
        Map<String, Object> responseMap = (Map<String, Object>) body.get("response");

        String providerUserId = (String) responseMap.get("id");
        String nicknameFromNaver = (String) responseMap.get("nickname");


        // 3. DB CHECK
        Optional<User> userOptional =
                userRepository.findByProviderAndProviderUserId(Provider.NAVER, providerUserId);

        // EXISTING USER
        if (userOptional.isPresent()) {

            User user = userOptional.get();

            // signup 필요
            if (user.getNickname() == null) {
                response.sendRedirect(
                        redirectUrl + "/signup?provider=NAVER&userId=" + providerUserId
                );
                return;
            }

            String accessToken =
                    jwtProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole());

            cookieUtil.addAccessTokenCookie(response, accessToken);

            response.sendRedirect(redirectUrl + "/");
            return;
        }
        User newUser = User.builder()
                .provider(Provider.NAVER)
                .providerUserId(providerUserId)
                .nickname(null)
                .profileImageName("default.png")
                .role(com.boogle.entity.type.Role.USER)
                .build();

        userRepository.save(newUser);

        System.out.println("user saved");
        System.out.println("REDIRECT -> SIGNUP");

        response.sendRedirect(
                redirectUrl + "/signup?provider=NAVER&userId=" + providerUserId
        );
    }
}