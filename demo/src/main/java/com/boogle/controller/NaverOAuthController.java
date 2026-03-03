package com.boogle.controller;

import com.boogle.component.NaverProperties;
import com.boogle.dto.NicknameRequestDto;
import com.boogle.entity.User;
import com.boogle.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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