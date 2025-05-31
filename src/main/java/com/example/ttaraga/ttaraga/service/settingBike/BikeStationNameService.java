package com.example.ttaraga.ttaraga.service.settingBike;

import com.example.ttaraga.ttaraga.dto.StationNameResponseDto;
import com.example.ttaraga.ttaraga.entity.Bike;
import com.example.ttaraga.ttaraga.repository.BikeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BikeStationNameService {
    private final BikeRepository bikeRepository;

    public BikeStationNameService(BikeRepository bikeRepository) {
        this.bikeRepository = bikeRepository;
    }

    public StationNameResponseDto getStationNameByStationId(String stationid) {
        if(stationid==null || stationid.trim().isEmpty()){
            throw new IllegalArgumentException("대여소 이름이 비어있습니다.");
        }

        Optional<Bike> stationOpt = bikeRepository.findById(stationid);
        Bike bikestation = stationOpt.orElseThrow(() ->
                new IllegalArgumentException("대여소의 이름이 올바르지 않습니다: " + stationid));

        return new StationNameResponseDto(
                bikestation.getStationLatitude(),
                bikestation.getStationLongitude()
        );
    }
}
