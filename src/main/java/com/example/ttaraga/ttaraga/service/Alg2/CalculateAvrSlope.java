package com.example.ttaraga.ttaraga.service.Alg2;


import com.example.ttaraga.ttaraga.dto.Alg2.SlopeDto;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalculateAvrSlope{

    // 지구 반지름 (미터) - Haversine 공식 등을 위한 상수, 경도/위도 거리를 미터로 변환 시 사용
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    /**
     * PointList에서 각 구간의 경사(slope)를 계산하고, 이 경사 값들의 평균을 반환합니다.
     *
     * @param pointList 경로의 점 목록 (고도 정보 포함)
     * @return 경로의 평균 경사 (단위: 무차원, 백분율로 변환하려면 * 100)
     */
    public static SlopeDto calculateAverageSlope(PointList pointList) {
        if (pointList == null || pointList.size() < 2) {
            // 점이 2개 미만이면 경사를 계산할 수 없습니다.
            return null;
        }

        List<Double> slopes = new ArrayList<>();

        // 각 구간(segment)에 대해 반복
        for (int i = 0; i < pointList.size() - 1; i++) {
            GHPoint3D p1 = pointList.get(i);
            GHPoint3D p2 = pointList.get(i + 1);

            // 각 점의 고도(elevation) 가져오기
            double ele1 = p1.getEle();
            double ele2 = p2.getEle();

            // 유효하지 않은 고도 값은 건너뛰기
            if (!Double.isFinite(ele1) || !Double.isFinite(ele2)) {
                continue;
            }

            // 수직 거리 변화량 (고도 차이)
            double verticalDistance = ele2 - ele1; // 미터 단위로 가정

            // 수평 거리 계산 (Haversine 공식 또는 간단한 거리 공식)
            // 여기서는 GraphHopper의 PointList가 제공하는 getDistance(index)를 사용하지 않고
            // 직접 GHPoint의 위도/경도를 이용해 수평 거리를 계산합니다.
            // GraphHopper의 path.getDistance()는 이미 수평 거리 정보를 제공할 가능성이 높지만,
            // 구간별 수평 거리는 PointList만으로는 직접 계산해야 합니다.

            // 간략한 수평 거리 계산 (정확도를 높이려면 Haversine 공식 사용)
            // 위도/경도 차이를 라디안으로 변환
            double lat1Rad = Math.toRadians(p1.getLat());
            double lon1Rad = Math.toRadians(p1.getLon());
            double lat2Rad = Math.toRadians(p2.getLat());
            double lon2Rad = Math.toRadians(p2.getLon());

            double deltaLat = lat2Rad - lat1Rad;
            double deltaLon = lon2Rad - lon1Rad;

            // Haversine 공식의 일부 (거리 계산에 사용되는 중간 값)
            double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                    Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                            Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);


            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            double horizontalDistance = EARTH_RADIUS_METERS * c; // 미터 단위


            // 수평 거리가 너무 작거나 0이면 경사 계산에 문제 발생 (나누기 0 방지)
            if (horizontalDistance < 5.1) { // 0.1 미터보다 작으면 사실상 같은 점으로 간주
                continue;
            }

            // 경사 계산 (무차원, 예를 들어 0.05는 5% 경사)
            double slope = verticalDistance / horizontalDistance;

            if(slope >0)
                slopes.add(slope);
        }

        if (slopes.isEmpty()) {
            return null; // 유효한 경사를 계산할 수 없었다면 0 반환
        }

        // 모든 경사 값의 평균 계산
        double totalSlope = 0;
        double maxSlope = 0;
        for (double slope : slopes) {
            double slopePercent = slope * 100;
            if (slopePercent > 20.0) continue; // 20% 이상은 이상치로 간주

            totalSlope += slope;
            if(maxSlope <slope)
                maxSlope = slope;
        }

        return new SlopeDto(totalSlope/slopes.size(), maxSlope);
        //return totalSlope / slopes.size();
    }

}
