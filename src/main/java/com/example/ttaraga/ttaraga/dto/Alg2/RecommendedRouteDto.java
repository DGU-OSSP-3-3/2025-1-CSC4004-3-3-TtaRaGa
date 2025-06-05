package com.example.ttaraga.ttaraga.dto.Alg2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok 어노테이션으로 getter, setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor // Lombok 어노테이션으로 기본 생성자 자동 생성
@AllArgsConstructor // Lombok 어노테이션으로 모든 필드를 포함하는 생성자 자동 생성
public class RecommendedRouteDto {
    private String point1Id;         // 첫 번째 경유지 ID
    private String point1Name;     // 첫 번째 경유지 이름
    private double point1Lat;      // 첫 번째 경유지 위도
    private double point1Lon;      // 첫 번째 경유지 경도

    private String point2Id;         // 두 번째 경유지 ID
    private String point2Name;     // 두 번째 경유지 이름
    private double point2Lat;      // 두 번째 경유지 위도
    private double point2Lon;      // 두 번째 경유지 경도

    private double actualRouteDistanceM; // GraphHopper가 계산한 실제 주행 거리 (미터)
    private double areaSqM;              // 경로가 만드는 면적 (제곱미터)
    private String geoJsonPath;          // 경로를 나타내는 GeoJSON (또는 간단한 PointList 문자열)
}