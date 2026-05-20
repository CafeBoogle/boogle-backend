package com.boogle.repository;

import com.boogle.dto.projection.CafeListProjection;
import com.boogle.entity.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    Optional<Cafe> findByKakaoPlaceId(String kakaoPlaceId); // 카카오맵에서의 카페 고유 id로 찾기
    // 위도와 경도가 각각 특정범위(카카오맵 표시지역)사이에 있는 데이터 찾기
    List<Cafe> findByLatitudeBetweenAndLongitudeBetween(Double minLat, Double maxLat, Double minLng, Double maxLng);

    // 카카오 위치ID로 장소 중복 제거
    List<Cafe> findByKakaoPlaceIdIn(List<String> kakaoPlaceIds);

    @Query("""
    SELECT
        c.id as id,
        c.name as name,
        c.address as address,
        c.latitude as latitude,
        c.longitude as longitude,
        c.imageName as thumbnail,
        COUNT(r.id) as reviewCount
    FROM Cafe c
    LEFT JOIN Review r ON r.cafe.id = c.id
    WHERE c.latitude BETWEEN :minLat AND :maxLat
      AND c.longitude BETWEEN :minLng AND :maxLng
    GROUP BY
        c.id,
        c.name,
        c.address,
        c.latitude,
        c.longitude,
        c.imageName
    """)
    List<CafeListProjection> findCafeListWithinBounds(Double minLat, Double maxLat, Double minLng, Double maxLng);

}
