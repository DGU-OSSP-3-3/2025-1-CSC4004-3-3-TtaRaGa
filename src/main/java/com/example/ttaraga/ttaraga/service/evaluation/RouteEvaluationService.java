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
        Coordinate[] coords = new Coordinate[points.size()];
        for (int i = 0; i < points.size(); i++) {
            coords[i] = new Coordinate(points.getLon(i), points.getLat(i)); // GraphHopper는 (lon, lat) 순서
        }

        for (int i = 0; i < coords.length - 1; i++) {
            Coordinate a = coords[i];
            Coordinate b = coords[i + 1];

            LineString segment = geometryFactory.createLineString(new Coordinate[]{a, b});
            double segmentLength = segment.getLength();
            if (segmentLength == 0.0) continue;

            double slopeScore = 0.0;
            //slopeEvaluator.evaluate(segment);
            double sceneryScore = 0.0;
            // sceneryEvaluator.evaluate(segment);
            double bikeScore = bikePathEvaluator.evaluate(segment);

            double finalSegmentScore =
                    slopeScore * config.getSlopeWeight()
                            + sceneryScore * config.getSceneryWeight()
                            + bikeScore * config.getBikePathWeight();

            totalScore += finalSegmentScore * segmentLength;
            totalDistance += segmentLength;

        }

        if (totalDistance == 0) {
            throw new IllegalArgumentException("총 거리 0. 유효한 경로가 아닙니다.");
        }

        double routeScore = totalScore / totalDistance;
        System.out.printf("[TOTAL] 전체 거리: %.2fm | 전체 경로 점수: %.4f%n",
                totalDistance * 111_000, routeScore
        );

        return routeScore;
    }
    /**
     * 중간 좌표를 구해서 평가에 사용할 수 있도록 변환
     */

}