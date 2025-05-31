package com.example.ttaraga.ttaraga.dto;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@AllArgsConstructor
public class RouteResultDto {
    private String geoJson;                 // 전체 경로의 GeoJSON 표현
    private List<GHPoint> pathPoints;       // 출발지와 도착지
    @Getter
    private ResponsePath responsePath;      // 실제 경로 객체 (점수 계산용)

    public RouteResultDto(String geoJson, List<GHPoint> pathPoints) {
        this.geoJson = geoJson;
        this.pathPoints = pathPoints;
        this.responsePath = null; // 초기화 시점에는 ResponsePath가 없으므로 null로 설정
    }

    public ResponsePath getResponsePath() {
        return responsePath;
    }
}