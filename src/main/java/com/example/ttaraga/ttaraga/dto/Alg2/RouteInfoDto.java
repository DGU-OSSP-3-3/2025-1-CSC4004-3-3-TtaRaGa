package com.example.ttaraga.ttaraga.dto.Alg2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteInfoDto {
    private String geoJson;   // 경로를 GeoJSON 형식으로 직렬화한 문자열
    private double distance;  // 경로 거리 (단위: m)
    private double duration;  // 예상 소요시간 (단위: 초)
    private double averageSlope;
    private double maxSlope;

}
