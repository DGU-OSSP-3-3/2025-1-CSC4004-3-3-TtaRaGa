package com.example.ttaraga.ttaraga;

import com.example.ttaraga.ttaraga.service.RouteEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RouteEvaluationServiceTest {

    @Autowired
    private RouteEvaluationService routeEvaluationService;

    @Test
    public void testEvaluateRoute() {
        String geoJson = "...";
        double score = routeEvaluationService.evaluateRoute(geoJson);
        System.out.println("점수: " + score);
    }

}
