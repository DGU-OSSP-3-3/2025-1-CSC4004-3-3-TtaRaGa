package com.example.ttaraga.ttaraga.service;// src/test/java/com/example/ttaraga/ttaraga/service/RouteEvaluationUnitTest.java

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RouteEvaluationUnitTest {

    @Test
    void testEvaluateRoute() {
        RouteEvaluationService service = new RouteEvaluationService();

        String dummyGeoJson = """
        {
          "type": "Feature",
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [127.0, 37.5],
              [127.01, 37.51]
            ]
          }
        }
        """;

        double score = service.evaluateRoute(dummyGeoJson);
        System.out.println("점수: " + score);
        assertTrue(score >= 0);
    }
}