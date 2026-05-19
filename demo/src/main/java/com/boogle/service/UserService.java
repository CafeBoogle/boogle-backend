package com.boogle.service;

import com.boogle.dto.request.SignUpRequest;
import com.boogle.entity.User;
import com.boogle.entity.type.Provider;
import com.boogle.entity.type.Role;
import com.boogle.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Value("${file.upload-dir.profile}")
    private String uploadPath;

    @Transactional
    public User updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setNickname(nickname);
        user.setRole(Role.USER);
        return user;
    }

    @Transactional
    public User processKakaoUser(String providerUserId, String kakaoNickname) {
        return userRepository.findByProviderAndProviderUserId(Provider.KAKAO, providerUserId)
                .orElseGet(() -> {
                    String finalNickname = kakaoNickname;
                    if (userRepository.existsByNickname(finalNickname)) {
                        finalNickname = "Temp_" + UUID.randomUUID().toString().substring(0, 8);
                    }

                    User newUser = User.builder()
                            .provider(Provider.KAKAO)
                            .providerUserId(providerUserId)
                            .nickname(finalNickname)
                            .role(Role.USER)
                            .profileImageName("default.png")
                            .build();
                    return userRepository.save(newUser);
                });
    }

    @Transactional
    public User signUp(SignUpRequest request) {
        User user = userRepository.findByProviderAndProviderUserId(request.getProvider(), request.getProviderUserId())
                .orElseGet(() -> {
                    if (userRepository.existsByNickname(request.getNickname())) {
                        throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
                    }
                    return User.builder()
                            .provider(request.getProvider())
                            .providerUserId(request.getProviderUserId())
                            .nickname(request.getNickname())
                            .profileImageName(request.getCatId())
                            .role(Role.USER)
                            .build();
                });

        return userRepository.save(user);
    }
}