package com.boogle.dto;

import com.boogle.entity.Cafe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CafeSaveRequestDto {

    @NotBlank(message = "장소ID는 필수입니다.")
    private String kakaoPlaceId;
    @NotBlank(message = "카페 이름은 필수입니다.")
    private String name;
    @NotBlank(message = "카페 주소는 필수입니다.")
    private String address;
    @NotNull(message = "위도 값은 필수입니다.")
    private Double latitude;
    @NotNull(message = "경도 값은 필수입니다.")
    private Double longitude;

    public Cafe toEntity() {
        return Cafe.builder()
                .kakaoPlaceId(kakaoPlaceId)
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
