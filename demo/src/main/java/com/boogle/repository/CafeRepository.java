package com.boogle.repository;

import com.boogle.entity.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    Optional<Cafe> findByKakaoPlaceId(String kakaoPlaceId); // 카카오맵에서의 카페 고유 id로 찾기
    // 위도와 경도가 각각 특정범위(카카오맵 표시지역)사이에 있는 데이터 찾기
    List<Cafe> findByLatitudeBetweenAndLongitudeBetween(Double minLat, Double maxLat, Double minLng, Double maxLng);
}
