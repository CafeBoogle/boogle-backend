package com.boogle.service;

import com.boogle.entity.User;
import com.boogle.entity.type.Role;
import com.boogle.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

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

}
