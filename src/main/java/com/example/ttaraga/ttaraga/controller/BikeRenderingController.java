package com.example.ttaraga.ttaraga.controller;

import com.example.ttaraga.ttaraga.dto.BikeRequestDto;
import com.example.ttaraga.ttaraga.repository.BikeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bike-stations")
public class BikeRenderingController {
    private final BikeRepository bikeRepository;

    public BikeRenderingController(BikeRepository bikeRepository) {
        this.bikeRepository = bikeRepository;
    }

    @PostMapping
    public ResponseEntity<List<Object[]>> getStation(@RequestBody BikeRequestDto requestDto) {
        try{
            List<Object[]> response = bikeRepository.findStationsInBounds(
                    requestDto.lat1(), requestDto.lng1(), requestDto.delta1(), requestDto.delta2());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch(Exception e){
            return ResponseEntity.status(500).body(null);
        }
    }
}
