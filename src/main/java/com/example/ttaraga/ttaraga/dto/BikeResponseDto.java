package com.example.ttaraga.ttaraga.dto;

import com.example.ttaraga.ttaraga.dto.BikeDto;
import java.util.List;

public record BikeResponseDto(
        Long stationCount,
        List<BikeDto> stations
){
    public record BikeDto(
        String stationId,
        long parkingBikeToCnt,
        double stationLatitude,
        double stationLongitude,
        String stationName
    ){}
}
