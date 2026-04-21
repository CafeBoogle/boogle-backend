package com.boogle.repository;

import com.boogle.dto.projection.CafeScoreProjection;
import com.boogle.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Objects;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 카페의 모든 리뷰 조회 (최신순)
    List<Review> findByCafeIdOrderByCreatedAtDesc(Long cafeId);

    // 특정 사용자가 작성한 리뷰 조회
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);



    @Query("""
    SELECT new com.boogle.dto.projection.CafeScoreProjection(
        r.cafe.id,
        AVG(r.toiletScore),
        AVG(r.outletScore),
        AVG(r.seatScore),
        AVG(r.wifiScore),
        AVG(r.noiseScore),
        AVG(r.studyScore),
        COUNT(r.id)
    )
    FROM Review r
    WHERE r.cafe.id = :cafeId
    GROUP BY r.cafe.id
""")
    CafeScoreProjection findCafeScoreByCafeId(
            @Param("cafeId") Long cafeId
    );



    @Query("""
            SELECT new com.boogle.dto.projection.CafeScoreProjection(
                r.cafe.id,
                AVG(r.toiletScore),
                AVG(r.outletScore),
                AVG(r.seatScore),
                AVG(r.wifiScore),
                AVG(r.noiseScore),
                AVG(r.studyScore),
                COUNT(r.id)
            )
            FROM Review r
            WHERE r.cafe.id IN :cafeIds
            GROUP BY r.cafe.id
        """)
    List<CafeScoreProjection> findCafeScoresByCafeIds(
            @Param("cafeIds") List<Long> cafeIds
    );

}