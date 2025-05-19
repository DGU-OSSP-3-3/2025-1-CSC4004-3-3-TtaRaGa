package com.example.ttaraga.ttaraga.dto;

import lombok.Data;

//따릉이 대여소 위치 데이터 관리를 위한 Dto
@Data
public class BikeDto {
    private long stationId;
    private long parkingBikeTotCnt;
    private double stationLatitude;
    private double stationLongitude;
    private String stationName;
}

