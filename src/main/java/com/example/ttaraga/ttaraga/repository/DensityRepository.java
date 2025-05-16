package com.example.ttaraga.ttaraga.repository;

import com.example.ttaraga.ttaraga.entity.Density;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DensityRepository extends JpaRepository<Density, Long> {
}
