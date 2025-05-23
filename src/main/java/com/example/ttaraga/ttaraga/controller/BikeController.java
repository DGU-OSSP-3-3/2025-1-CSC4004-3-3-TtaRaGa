package com.example.ttaraga.ttaraga.controller;

import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.service.settingBike.BikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BikeController {
    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    // 모든 따릉이 대여소 정보를 반환하는 GET 요청 처리
    @GetMapping
    public ResponseEntity<List<BikeDto>> getAllBikesInfo() {
        List<BikeDto> bikedtos = bikeService.getAllBikes();
        return ResponseEntity.ok(bikedtos);
    }

    // 특정 ID의 자전거 대여소 정보를 반환하는 GET 요청 처리
    @GetMapping("/{id}")
    public ResponseEntity<BikeDto> getBikeStationById(@PathVariable Long id) {
        BikeDto bikeStation = bikeService.getBikeStationById(id);
        if (bikeStation != null) {
            return ResponseEntity.ok(bikeStation); // 200 OK 응답과 함께 DTO 반환
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found 응답
        }
    }
}
