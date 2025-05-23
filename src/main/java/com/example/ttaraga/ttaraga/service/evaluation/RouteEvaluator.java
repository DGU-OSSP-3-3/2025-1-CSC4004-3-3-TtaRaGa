package com.example.ttaraga.ttaraga.service.evaluation;

import org.locationtech.jts.geom.Coordinate;

public interface RouteEvaluator {
    double evaluate(Coordinate coord);
}