package com.example.ttaraga.ttaraga.service.evaluation;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

// SlopeEvaluator.java
@Component
public class SlopeEvaluator implements RouteEvaluator {

    // 기존: 좌표 기반 경사도 평가
    @Override
    public double evaluate(Coordinate coord) {
        return 0.1; // 임시값, 실제 경사 계산 로직 대체 필요
    }

    // 추가: LineString 기반 평가 (중간점 기준)
    public double evaluate(LineString segment) {
        Coordinate center = segment.getCentroid().getCoordinate();
        return evaluate(center);
    }
}