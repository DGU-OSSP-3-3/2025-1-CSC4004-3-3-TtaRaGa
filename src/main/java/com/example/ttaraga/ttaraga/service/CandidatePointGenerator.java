package com.example.ttaraga.ttaraga.service;

import com.graphhopper.util.shapes.GHPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CandidatePointGenerator {
    public List<GHPoint> generate(GHPoint center, double radiusMeters) {
        List<GHPoint> points = new ArrayList<>();
        int numPoints = 90; // 총 생성할 방향 수 ex) 90개
        double angleStep = 360.0 / numPoints; // 각도 간격 ex) 4도

        for (int i = 0; i < numPoints; i++) {
            double angle = i * angleStep;
            double rad = Math.toRadians(angle);

            double latOffset = (radiusMeters / 111320.0) * Math.cos(rad);
            double lonOffset = (radiusMeters / (111320.0 * Math.cos(Math.toRadians(center.getLat())))) * Math.sin(rad);

            double lat = center.getLat() + latOffset;
            double lon = center.getLon() + lonOffset;

            points.add(new GHPoint(lat, lon));
        }

        return points;
    }
}