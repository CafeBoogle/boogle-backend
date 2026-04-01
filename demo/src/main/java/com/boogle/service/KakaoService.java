package com.boogle.service;

import com.boogle.entity.User;
import com.boogle.entity.type.Provider;
import com.boogle.entity.type.Role;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
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


    @Autowired
    private Environment env;

    @PostConstruct
    public void whereIsMyPropertyFrom() {
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;

        System.out.println("===== PROPERTY SOURCE TRACE =====");
        for (PropertySource<?> ps : ce.getPropertySources()) {
            if (ps.containsProperty("kakao.redirect-uri")
                    || ps.containsProperty("KAKAO_REDIRECT_URI")) {

                System.out.println("✅ FOUND IN: " + ps.getName());
                System.out.println("   kakao.redirect-uri = "
                        + ps.getProperty("kakao.redirect-uri"));
                System.out.println("   KAKAO_REDIRECT_URI = "
                        + ps.getProperty("KAKAO_REDIRECT_URI"));
            }
        }
        System.out.println("=================================");
    }




    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;

    public void login(String code, HttpServletResponse response) throws IOException {

        // 1️⃣ 카카오 access token 발급
        String kakaoAccessToken = getKakaoAccessToken(code);

        // 2️⃣ 사용자 정보 조회
        Map<String, Object> body = getKakaoUserInfo(kakaoAccessToken);
        String providerUserId = String.valueOf(body.get("id"));

        // [추가] 카카오 properties에서 닉네임 가져오기
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        String kakaoNickname = (properties != null) ? (String) properties.get("nickname") : null;

        // 3️⃣ DB 조회
        Optional<User> optionalUser = userRepository.findByProviderAndProviderUserId(
                Provider.KAKAO,
                providerUserId
        );

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // 기존 유저인데 닉네임이 없다면 (이전 가입 실패 등) 카카오 닉네임으로 업데이트 시도 가능
            if (user.getNickname() == null) {
                issueTempToken(user, response);
                response.sendRedirect(frontendUrl + "/signup");
            } else {
                issueFullToken(user, response);
                response.sendRedirect(frontendUrl + "/category");
            }
        } else {
            // 4️⃣ 신규 가입 시 카카오 닉네임 적용
            User newUser = User.builder()
                    .provider(Provider.KAKAO)
                    .providerUserId(providerUserId)
                    .nickname(kakaoNickname != null ? kakaoNickname : "TempUser") // null 방지
                    .role(Role.USER)
                    .profileImageName("default.png")
                    .build();

            userRepository.save(newUser);

            issueTempToken(newUser, response);
            response.sendRedirect(frontendUrl + "/signup");
        }
    }
    private String getKakaoAccessToken(String code) {
        System.out.println("========= [DEBUG] 카카오 토큰 요청 시작 =========");
        System.out.println("인가 코드: " + code);
        System.out.println("Redirect URI: " + redirectUri);

        RestTemplate restTemplate = new RestTemplate();

        // 1. 헤더 설정 (반드시 FORM_URLENCODED)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Accept", "application/json");

        // 2. 파라미터 설정 (순서를 카카오 가이드에 맞게 조정)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret); // 보안 설정 ON인 경우 필수

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token",
                    request,
                    Map.class
            );

            System.out.println("========= [DEBUG] 카카오 응답 성공 =========");
            return (String) response.getBody().get("access_token");

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 🚨 여기서 401의 진짜 이유(KOE006 등)가 출력됩니다!
            System.err.println("========= [ERROR] 카카오 토큰 발급 실패 =========");
            System.err.println("상태 코드: " + e.getStatusCode());
            System.err.println("에러 본문: " + e.getResponseBodyAsString());
            throw e;
        }
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