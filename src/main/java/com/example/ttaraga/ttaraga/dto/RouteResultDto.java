package com.example.ttaraga.ttaraga.dto;

import com.graphhopper.util.shapes.GHPoint;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RouteResultDto {
    private String geoJson;           // 전체 경로의 GeoJSON 표현
    private List<GHPoint> pathPoints; // 출발지와 도착지
}