package com.boogle.service;

import com.boogle.dto.request.SignUpRequest;
import com.boogle.entity.User;
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
    @Value("${file.upload-dir}")
    private String uploadPath;
    @Transactional
    public User updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 닉네임 설정
        user.setNickname(nickname);
        user.setRole(Role.USER);

        return user;
    }
    @Transactional
    public void signUp(SignUpRequest request) {
        // 1. 이미 존재하는 유저인지 먼저 확인 (카카오 로그인 시 자동 생성되었을 가능성)
        User user = userRepository.findByProviderAndProviderUserId(request.getProvider(), request.getProviderUserId())
                .orElseGet(() -> User.builder() // 없으면 새로 생성
                        .provider(request.getProvider())
                        .providerUserId(request.getProviderUserId())
                        .build());

        // 2. 파일 저장 로직
        String fileName = user.getProfileImageName() != null ? user.getProfileImageName() : "default.png";

        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            File directory = new File(uploadPath);
            if (!directory.exists()) directory.mkdirs();

            fileName = UUID.randomUUID() + "_" + request.getProfileImage().getOriginalFilename();

            try {
                File saveFile = new File(directory.getAbsolutePath() + File.separator + fileName);
                request.getProfileImage().transferTo(saveFile);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 중 오류 발생: " + e.getMessage());
            }
        }

        // 3. 기존 유저 정보 업데이트 (또는 새 유저 정보 완성)
        user.setNickname(request.getNickname());
        user.setProfileImageName(fileName);
        user.setRole(Role.USER);

        // 4. 저장 (JPA가 알아서 ID가 있으면 Update, 없으면 Insert 합니다)
        userRepository.save(user);
    }
}
