package com.boogle.dto.request;

import com.boogle.entity.type.Provider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SignUpRequest {
    private Provider provider;        // KAKAO 등
    private String providerUserId;    // 소셜 고유 ID
    private String nickname;          // 닉네임
//    private MultipartFile profileImage; // 프로필 사진 파일
    private String catId; //변경: 고유 고양이 ID (SG, Y, H, E )

}