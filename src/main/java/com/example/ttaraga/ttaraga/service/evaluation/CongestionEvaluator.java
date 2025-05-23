package com.example.ttaraga.ttaraga.service.evaluation;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

// CongestionEvaluator.java
@Component
public class CongestionEvaluator implements RouteEvaluator {
    @Override
    public double evaluate(Coordinate coord) {
        return 0.3;
    }
}
