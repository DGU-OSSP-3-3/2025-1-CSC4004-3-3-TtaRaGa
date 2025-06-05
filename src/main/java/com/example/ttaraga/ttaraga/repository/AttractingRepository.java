package com.example.ttaraga.ttaraga.repository;

import com.example.ttaraga.ttaraga.dto.Alg2.RouteCandidateDto;
import com.example.ttaraga.ttaraga.entity.AttractingPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttractingRepository extends JpaRepository<AttractingPlace, String> {
    // 복잡한 SQL 쿼리를 직접 작성하여 경로 후보 지점 세트(두 지점)를 조회
    // nativeQuery = true로 설정하여 원시 SQL 쿼리 사용
    @Query(value = """
        SELECT
          A.place_id AS point1_id,
          A.place_name AS point1_name,
          A.place_latitude AS point1_lat,
          A.place_longitude AS point1_lon,
          B.place_id AS point2_id,
          B.place_name AS point2_name,
          B.place_latitude AS point2_lat,
          B.place_longitude AS point2_lon,
          -- 총 거리
          (
            ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(A.place_longitude, A.place_latitude)) +
            ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude)) +
            ST_Distance_Sphere(POINT(B.place_longitude, B.place_latitude), POINT(:startLon, :startLat))
          ) AS total_distance_m
        FROM ttaraga_db.attracting_place A
        JOIN ttaraga_db.attracting_place B
          ON A.place_id < B.place_id
        WHERE
          -- 총 거리 제한
          (
            ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(A.place_longitude, A.place_latitude)) +
            ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude)) +
            ST_Distance_Sphere(POINT(B.place_longitude, B.place_latitude), POINT(:startLon, :startLat))
          ) BETWEEN :minTotalDistance AND :maxTotalDistance
          -- 출발점 기준 각 ≥ 20도 (cos ≤ 0.9397)
          AND (
            (
              POW(ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(A.place_longitude, A.place_latitude)), 2) +
              POW(ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(B.place_longitude, B.place_latitude)), 2) -
              POW(ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude)), 2)
            )
            / (
              2 *
              ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(A.place_longitude, A.place_latitude)) *
              ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(B.place_longitude, B.place_latitude))
            )
          ) <= 0.9397
          -- 🔽 예각삼각형 조건 추가: 세 각 모두 cos > 0
          AND (
            -- ∠출-A-B
            (
              POW(ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(A.place_longitude, A.place_latitude)), 2) +
              POW(ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude)), 2) -
              POW(ST_Distance_Sphere(POINT(B.place_longitude, B.place_latitude), POINT(:startLon, :startLat)), 2)
            )
            / (
              2 *
              ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(A.place_longitude, A.place_latitude)) *
              ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude))
            )
          ) > 0
          AND (
            -- ∠출-B-A
            (
              POW(ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(B.place_longitude, B.place_latitude)), 2) +
              POW(ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude)), 2) -
              POW(ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(A.place_longitude, A.place_latitude)), 2)
            )
            / (
              2 *
              ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(B.place_longitude, B.place_latitude)) *
              ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude))
            )
          ) > 0
          AND (
            -- ∠A-B-출
            (
              POW(ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude)), 2) +
              POW(ST_Distance_Sphere(POINT(B.place_longitude, B.place_latitude), POINT(:startLon, :startLat)), 2) -
              POW(ST_Distance_Sphere(POINT(:startLon, :startLat), POINT(A.place_longitude, A.place_latitude)), 2)
            )
            / (
              2 *
              ST_Distance_Sphere(POINT(A.place_longitude, A.place_latitude), POINT(B.place_longitude, B.place_latitude)) *
              ST_Distance_Sphere(POINT(B.place_longitude, B.place_latitude), POINT(:startLon, :startLat))
            )
          ) > 0
        ORDER BY total_distance_m ASC
        LIMIT :limit
        """, nativeQuery = true)

    List<RouteCandidateDto> findRouteCandidates(
            @Param("startLon") double startLon,
            @Param("startLat") double startLat,
            @Param("minTotalDistance") double minTotalDistance,
            @Param("maxTotalDistance") double maxTotalDistance,
            @Param("limit") int limit
    );
}