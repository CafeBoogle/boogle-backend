package controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import service.KakaoService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class KakaoOAuthController {

    private final KakaoService kakaoService;

    @GetMapping("/api/oauth/kakao/callback")
    public void kakaoCallback(@RequestParam String code,
                              HttpServletResponse response) throws IOException {

        kakaoService.login(code, response);
    }
}