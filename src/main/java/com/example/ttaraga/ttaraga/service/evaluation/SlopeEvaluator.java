package com.example.ttaraga.ttaraga.service.evaluation;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

// SlopeEvaluator.java
@Component
public class SlopeEvaluator implements RouteEvaluator {
    @Override
    public double evaluate(Coordinate coord) {
        return 0.1; // 임시값
    }
}
