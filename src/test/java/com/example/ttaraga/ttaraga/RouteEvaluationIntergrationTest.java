
package com.example.ttaraga.ttaraga;

import com.example.ttaraga.ttaraga.service.evaluation.RouteEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest // 실제 애플리케이션을 띄움 (context load)
class RouteEvaluationIntegrationTest {

    @Autowired
    private RouteEvaluationService routeEvaluationService;

    @Test
    void testEvaluateRoute_withValidGeoJson() {
        String geoJson = """
            {
              "type": "Feature",
              "geometry": {
                "type": "LineString",
                "coordinates": [
                  [126.9783881, 37.5666102],
                  [126.9793881, 37.5676102]
                ]
              },
              "properties": {}
            }
        """;

        double result = routeEvaluationService.evaluateRoute(geoJson);

        // 예상 점수 범위로 체크 (각 evaluator 점수 합산 결과)
        assertThat(result).isGreaterThan(0.0); // evaluator 로직 따라 달라짐
    }
}