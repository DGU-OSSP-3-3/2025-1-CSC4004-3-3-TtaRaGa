package com.example.ttaraga.ttaraga.dto;

import java.util.List;

public record BikeResponseDto(
        Long stationCount,
        List<ResponseBikeDto> stations
){
    public record ResponseBikeDto(
        String stationId,
        long parkingBikeToCnt,
        double stationLatitude,
        double stationLongitude,
        String stationName
    ){}
}
