package com.example.ttaraga.ttaraga.repository;

import com.example.ttaraga.ttaraga.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BikeRepository extends JpaRepository<Bike, Long> {
}
