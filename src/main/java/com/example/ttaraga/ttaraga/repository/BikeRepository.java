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
    Optional<Bike> findByStationName(String stationName);

    @Query(value = """
        SELECT station_id, parking_bike_tot_cnt, station_latitude, station_longitude, station_name, COUNT(*) over() as station_count
        FROM bike
        WHERE station_latitude BETWEEN LEAST(:lat1, :lat1 + :delta1) AND GREATEST(:lat1, :lat1 + :delta1)
          AND station_longitude BETWEEN LEAST(:lng1, :lng1 + :delta2) AND GREATEST(:lng1, :lng1 + :delta2)
        """, nativeQuery = true)
List<Object[]> findStationsInBounds(Double lat1, Double lng1, Double delta1, Double delta2);
}