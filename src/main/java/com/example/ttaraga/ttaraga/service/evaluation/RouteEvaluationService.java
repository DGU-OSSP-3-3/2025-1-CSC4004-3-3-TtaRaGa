package com.example.ttaraga.ttaraga.service.evaluation;

import com.example.ttaraga.ttaraga.config.RouteEvaluationConfig;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import com.graphhopper.ResponsePath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

@Service
public class RouteEvaluationService {

    private final SlopeEvaluator slopeEvaluator;
    private final SceneryEvaluator sceneryEvaluator;
    private final CongestionEvaluator congestionEvaluator;
    private final WeatherEvaluator weatherEvaluator;
    private final BikePathEvaluator bikePathEvaluator;
    private final RouteEvaluationConfig config;

    public RouteEvaluationService(
            SlopeEvaluator slopeEvaluator,
            SceneryEvaluator sceneryEvaluator,
            CongestionEvaluator congestionEvaluator,
            WeatherEvaluator weatherEvaluator,
            BikePathEvaluator bikePathEvaluator,
            RouteEvaluationConfig config
    ) {
        this.slopeEvaluator = slopeEvaluator;
        this.sceneryEvaluator = sceneryEvaluator;
        this.congestionEvaluator = congestionEvaluator;
        this.weatherEvaluator = weatherEvaluator;
        this.bikePathEvaluator = bikePathEvaluator;
        this.config = config;
    }

    /**
     * 🚲 ResponsePath 기반 경로 평가
     * - 각 Edge 단위로 거리, 평가값을 계산해 가중 평균 점수 반환
     */
    public double evaluateRoute(ResponsePath path) {
        double totalScore = 0.0;
        double totalDistance = 0.0;

        PointList points = path.getPoints();
        if (points.size() < 2) {
            throw new IllegalArgumentException("경로 포인트가 부족합니다.");
        }

        GeometryFactory geometryFactory = new GeometryFactory();

        for (int i = 0; i < points.size() - 1; i++) {
            Coordinate a = new Coordinate(points.getLon(i), points.getLat(i));
            Coordinate b = new Coordinate(points.getLon(i + 1), points.getLat(i + 1));

            double segmentDistance = a.distance(b); // 유클리드 거리 (간략화)
            if (segmentDistance == 0) continue;

            double slopeScore = slopeEvaluator.evaluate(a) * config.getSlopeWeight();
            double sceneryScore = sceneryEvaluator.evaluate(a) * config.getSceneryWeight();

            // ✅ LineString 생성 후 평가
            LineString segment = geometryFactory.createLineString(new Coordinate[]{a, b});
            double bikeScore = bikePathEvaluator.evaluate(segment) * config.getBikePathWeight();

            double segmentScore = slopeScore - sceneryScore + bikeScore;

            totalScore += segmentScore * segmentDistance;
            totalDistance += segmentDistance;
            System.out.printf(
                    "[SCORE] 거리: %.1fm, 경사: %.2f, 경치: %.2f, 자전거도로: %.2f, 총합: %.2f\n",
                    segmentDistance, slopeScore, sceneryScore, bikeScore, segmentScore
            );
        }

        if (totalDistance == 0) {
            throw new IllegalArgumentException("총 거리 0. 유효한 경로가 아닙니다.");
        }


        return totalScore / totalDistance;
    }

    /**
     * 중간 좌표를 구해서 평가에 사용할 수 있도록 변환
     */

}