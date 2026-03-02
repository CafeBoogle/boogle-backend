package com.boogle.controller;

import com.boogle.component.NaverProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.boogle.service.NaverService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NaverOAuthController {

    private final NaverService naverService;
    private final NaverProperties naverProperties;

    @GetMapping("/oauth/naver/callback")
    public void naverCallback(@RequestParam String code,
                              HttpServletResponse response) throws IOException {

        naverService.login(code, response);
    }

    @GetMapping("/oauth/naver")
    public void naverLogin(HttpServletResponse response) throws IOException {
        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + naverProperties.getClientId()
                + "&redirect_uri=" + naverProperties.getRedirectUri()
                + "&state=random";

        response.sendRedirect(naverAuthUrl);
    }
}