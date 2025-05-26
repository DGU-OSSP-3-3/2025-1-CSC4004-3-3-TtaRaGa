//package com.example.ttaraga.ttaraga;
//
//import com.example.ttaraga.ttaraga.service.evaluation.RouteEvaluationService;
//
//public class test {
//    public static void main(String[] args) {
//        String geoJson = """
//            {
//              "type": "Feature",
//              "geometry": {
//                "type": "LineString",
//                "coordinates": [
//                  [126.9784, 37.5665],
//                  [126.982, 37.5651],
//                  [126.985, 37.5640]
//                ]
//              },
//              "properties": {}
//            }
//        """;
//
//        RouteEvaluationService evaluator = new RouteEvaluationService();
//        double score = evaluator.evaluateRoute(geoJson);
//
//        System.out.println("경로 평가 점수: " + score);
//    }
//}