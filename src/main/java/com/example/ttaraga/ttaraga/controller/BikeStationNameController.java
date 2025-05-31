package com.example.ttaraga.ttaraga.controller;

import com.example.ttaraga.ttaraga.dto.StationNameResponseDto;
import com.example.ttaraga.ttaraga.dto.StationNameRequestDto;
import com.example.ttaraga.ttaraga.service.settingBike.BikeStationNameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/station-name")
public class BikeStationNameController {
    private final BikeStationNameService bikeStationNameService;

    public BikeStationNameController(BikeStationNameService bikeStationNameService) {
        this.bikeStationNameService = bikeStationNameService;
    }

    @PostMapping("/point")
    public ResponseEntity<StationNameResponseDto> getBikeStationName(@RequestBody StationNameRequestDto stationNameRequestDto){
        try{
            StationNameResponseDto response = bikeStationNameService.getStationNameByStationId(stationNameRequestDto.stationName());
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(null);
        }catch(Exception e){
            return ResponseEntity.status(500).body(null);
        }
    }
}
