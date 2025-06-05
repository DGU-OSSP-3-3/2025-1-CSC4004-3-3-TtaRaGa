package com.example.ttaraga.ttaraga.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestRouteResultDto {
    private String geoJson;  // 경로 정보
    private double distance; // 총 거리 (m)
    private double duration; // 소요 시간 (초)
    private double averageSlope; //평균 경사
    private double maxSlope;
}