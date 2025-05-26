package com.example.ttaraga.ttaraga.service.evaluation;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MidpointCalculatorService {

    private final GraphHopper graphHopper;

    @Autowired
    public MidpointCalculatorService(GraphHopper graphHopper) {
        this.graphHopper = graphHopper;
    }

    public GHPoint findMidpoint(GHPoint start, double durationHours) {
        double averageSpeedKmh = 15.0;
        double targetDistance = averageSpeedKmh * (durationHours / 2.0); // 편도 거리(km)

        // 단순한 반경 원형 지점 하나 예시 (북쪽 방향으로 targetDistance만큼 이동한 지점)
        double deltaLat = (targetDistance / 111.0); // 위도 1도 ≈ 111km
        GHPoint candidate = new GHPoint(start.lat + deltaLat, start.lon);

        // 거리 계산 후 반환
        GHRequest request = new GHRequest(start, candidate).setProfile("bike");
        GHResponse response = graphHopper.route(request);

        if (response.hasErrors()) {
            throw new RuntimeException("경로 탐색 실패: " + response.getErrors());
        }

        double timeInHours = response.getBest().getTime() / (1000.0 * 60.0 * 60.0);

        if (Math.abs(timeInHours - durationHours / 2.0) <= 0.2) {
            return candidate;
        } else {
            // 여기선 하나만 확인했지만, 여러 후보를 비교해 최적점 선택 가능
            return candidate;
        }
    }

    public ResponsePath getPath(GHPoint from, GHPoint to) {
        GHRequest request = new GHRequest(from, to).setProfile("bike");
        GHResponse response = graphHopper.route(request);
        if (response.hasErrors()) {
            throw new RuntimeException("경로 탐색 실패: " + response.getErrors());
        }
        return response.getBest();
    }
}