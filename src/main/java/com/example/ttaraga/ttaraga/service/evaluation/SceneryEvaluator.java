package com.example.ttaraga.ttaraga.service.evaluation;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

// SceneryEvaluator.java
@Component
public class SceneryEvaluator implements RouteEvaluator {
    @Override
    public double evaluate(Coordinate coord) {
        return 0.2;
    }
}
