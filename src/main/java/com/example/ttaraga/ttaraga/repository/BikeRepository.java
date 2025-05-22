package com.example.ttaraga.ttaraga.repository;

import com.example.ttaraga.ttaraga.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BikeRepository extends JpaRepository<Bike, Long> {
    Optional<Bike> findByApiStationId(String apiStationId);
}
