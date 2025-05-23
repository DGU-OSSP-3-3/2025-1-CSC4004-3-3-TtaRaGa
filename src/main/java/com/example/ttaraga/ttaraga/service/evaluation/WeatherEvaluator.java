package com.example.ttaraga.ttaraga.service.evaluation;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

// WeatherEvaluator.java
@Component
public class WeatherEvaluator implements RouteEvaluator {
    @Override
    public double evaluate(Coordinate coord) {
        return 0.4;
    }
}
