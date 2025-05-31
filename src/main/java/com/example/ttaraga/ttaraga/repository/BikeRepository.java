package com.example.ttaraga.ttaraga.repository;

import com.example.ttaraga.ttaraga.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BikeRepository extends JpaRepository<Bike, String> {
//public interface BikeRepository extends JpaRepository<Bike, Long> {
//    Optional<Bike> findByApiStationId(String apiStationId);
    Optional<Bike> findByStationName(String stationName);  // 따릉이 대여소 id 제공 시 해당 대여소의 위도, 경도 값 반환.

    @Query(value = """
        SELECT station_id, parking_bike_tot_cnt, station_latitude, station_longitude, station_name, COUNT(*) over() as station_count
        FROM bike
        WHERE station_latitude BETWEEN LEAST(:lat1, :lat1 + :delta1) AND GREATEST(:lat1, :lat1 + :delta1)
          AND station_longitude BETWEEN LEAST(:lng1, :lng1 + :delta2) AND GREATEST(:lng1, :lng1 + :delta2)
        """, nativeQuery = true)  // 지도 랜더링을 위해 위도, 경도 범위 안의 대여소들 리스트를 반환하기 위한 쿼리문.
List<Object[]> findStationsInBounds(Double lat1, Double lng1, Double delta1, Double delta2);
}