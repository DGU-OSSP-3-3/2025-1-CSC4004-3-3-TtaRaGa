package com.example.ttaraga.ttaraga.service.evaluation;

import com.example.ttaraga.ttaraga.config.RouteEvaluationConfig;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.stereotype.Service;

import java.io.StringReader;

@Service
public class RouteEvaluationService {

    private final SlopeEvaluator slopeEvaluator;
    private final SceneryEvaluator sceneryEvaluator;
    private final CongestionEvaluator congestionEvaluator;
    private final WeatherEvaluator weatherEvaluator;
    private final RouteEvaluationConfig config;

    public RouteEvaluationService(
            SlopeEvaluator slopeEvaluator,
            SceneryEvaluator sceneryEvaluator,
            CongestionEvaluator congestionEvaluator,
            WeatherEvaluator weatherEvaluator,
            RouteEvaluationConfig config
    ) {
        this.slopeEvaluator = slopeEvaluator;
        this.sceneryEvaluator = sceneryEvaluator;
        this.congestionEvaluator = congestionEvaluator;
        this.weatherEvaluator = weatherEvaluator;
        this.config = config;
    }

    public double evaluateRoute(String geoJsonString) {
        try {
            FeatureJSON fjson = new FeatureJSON();
            SimpleFeature feature = fjson.readFeature(new StringReader(geoJsonString));
            Geometry geometry = (Geometry) feature.getDefaultGeometry();

            if (!(geometry instanceof LineString)) {
                throw new IllegalArgumentException("지원하지 않는 geometry 타입입니다: " + geometry.getGeometryType());
            }

            Coordinate[] coordinates = geometry.getCoordinates();
            double totalScore = 0.0;

            for (Coordinate coord : coordinates) {
                double score =
                        slopeEvaluator.evaluate(coord) * config.getSlopeWeight()
                                - sceneryEvaluator.evaluate(coord) * config.getSceneryWeight()
                                + congestionEvaluator.evaluate(coord) * config.getCongestionWeight()
                                + weatherEvaluator.evaluate(coord) * config.getWeatherWeight();

                totalScore += score;
            }

            return totalScore / coordinates.length;

        } catch (Exception e) {
            throw new RuntimeException("GeoJSON 경로 평가 중 오류 발생", e);
        }
    }
}