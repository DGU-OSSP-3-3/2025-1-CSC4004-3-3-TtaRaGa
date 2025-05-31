package com.example.ttaraga.ttaraga.service.evaluation;

import com.example.ttaraga.ttaraga.dto.RouteResultDto;
import com.example.ttaraga.ttaraga.service.Routing.RouteService;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RouteChainBuilder {

    private final RouteService routeService;
    private final RouteEvaluationService routeEvaluationService;

    public RouteChainBuilder(RouteService routeService, RouteEvaluationService routeEvaluationService) {
        this.routeService = routeService;
        this.routeEvaluationService = routeEvaluationService;
    }

    public RouteResultDto buildChainedRoute(Coordinate start, double totalTimeMinutes) {
        List<GHPoint> chainedPoints = new ArrayList<>();
        List<ResponsePath> chainedPaths = new ArrayList<>();
        JSONArray geoJsonFeatures = new JSONArray();

        GHPoint currentPoint = new GHPoint(start.y, start.x);
        chainedPoints.add(currentPoint);

        double stepTimeLimit = totalTimeMinutes * 0.25;
        int maxSteps = 4;

        System.out.printf("[START] 총 시간: %.2f분, 스텝당 시간 제한: %.2f분\n", totalTimeMinutes, stepTimeLimit);

        for (int i = 0; i < maxSteps; i++) {
            System.out.printf("[STEP %d] 현재 좌표: %.6f, %.6f\n", i + 1, currentPoint.getLat(), currentPoint.getLon());

            List<CandidateRoute> candidates = routeService.findCandidateRoutes(currentPoint, (int) stepTimeLimit);
            candidates.sort(Comparator.comparingDouble(CandidateRoute::getScore).reversed());

            boolean found = false;

            for (CandidateRoute candidate : candidates) {
                GHPoint nextPoint = candidate.getPoint();

                if (chainedPoints.size() >= 2) {
                    GHPoint p1 = chainedPoints.get(chainedPoints.size() - 2);
                    GHPoint p2 = chainedPoints.get(chainedPoints.size() - 1);
                    double angle = computeAngle(p1, p2, nextPoint);
                    System.out.printf("[DEBUG] 세 점 각도: %.2f도\n", angle);

                    if (angle < 30 || angle > 150) {
                        System.out.printf("[SKIP] 각도 조건 불만족 (%.2f도)\n", angle);
                        continue;
                    }
                }

                ResponsePath path = candidate.getPath();
                double timeMin = path.getTime() / 60000.0;

                chainedPoints.add(nextPoint);
                chainedPaths.add(path);
                geoJsonFeatures.add(parseGeoJsonFeature(candidate.getGeoJson()));
                currentPoint = nextPoint;

                System.out.printf("[CHAIN] 선택된 루트: %.2f분\n", timeMin);
                found = true;
                break;
            }

            if (!found) {
                System.out.println("[STOP] 조건을 만족하는 후보 경로 없음");
                break;
            }
        }

        // 기존 여러 feature의 GeoJSON을 문자열로 변환
        JSONObject fullGeoJson = new JSONObject();
        fullGeoJson.put("type", "FeatureCollection");
        fullGeoJson.put("features", geoJsonFeatures);
        String combinedGeoJson = fullGeoJson.toString();

        // coordinates만 합친 GeoJSON으로 변환
        String mergedGeoJson = mergeGeoJsonCoordinates(combinedGeoJson);

        ResponsePath last = chainedPaths.isEmpty() ? null : chainedPaths.get(chainedPaths.size() - 1);
        System.out.println("[FINISH] 최종 GeoJSON 및 포인트 반환 완료");
        return new RouteResultDto(mergedGeoJson, chainedPoints, last);
    }

    @SuppressWarnings("unchecked")
    public String mergeGeoJsonCoordinates(String geoJsonString) {
        JSONParser parser = new JSONParser();
        JSONArray mergedCoords = new JSONArray();

        try {
            JSONObject geoJson = (JSONObject) parser.parse(geoJsonString);
            JSONArray features = (JSONArray) geoJson.get("features");

            for (Object f : features) {
                JSONObject feature = (JSONObject) f;
                JSONObject properties = (JSONObject) feature.get("properties");
                JSONObject geom = (JSONObject) properties.get("the_geom");

                JSONArray coords = (JSONArray) geom.get("coordinates");
                for (Object coord : coords) {
                    mergedCoords.add(coord);  // 좌표 배열에 통합
                }
            }

            // 새로운 Feature 생성
            JSONObject geometry = new JSONObject();
            geometry.put("type", "LineString");
            geometry.put("coordinates", mergedCoords);

            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("properties", new JSONObject());
            feature.put("geometry", geometry);

            JSONArray finalFeatures = new JSONArray();
            finalFeatures.add(feature);

            JSONObject result = new JSONObject();
            result.put("type", "FeatureCollection");
            result.put("features", finalFeatures);

            return result.toJSONString();

        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private JSONObject parseGeoJsonFeature(String geoJson) {
        try {
            org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
            return (JSONObject) parser.parse(geoJson);
        } catch (Exception e) {
            System.out.println("[ERROR] GeoJSON 파싱 실패");
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private double computeAngle(GHPoint a, GHPoint b, GHPoint c) {
        double abX = b.lon - a.lon;
        double abY = b.lat - a.lat;
        double bcX = c.lon - b.lon;
        double bcY = c.lat - b.lat;

        double dot = abX * bcX + abY * bcY;
        double mag1 = Math.sqrt(abX * abX + abY * abY);
        double mag2 = Math.sqrt(bcX * bcX + bcY * bcY);
        if (mag1 == 0 || mag2 == 0) return 180.0;

        double cosTheta = dot / (mag1 * mag2);
        double angleRad = Math.acos(Math.max(-1, Math.min(1, cosTheta)));
        return Math.toDegrees(angleRad);
    }
}

//package com.example.ttaraga.ttaraga.service.evaluation;
//
//import com.example.ttaraga.ttaraga.dto.RouteResultDto;
//import com.example.ttaraga.ttaraga.service.Routing.RouteService;
//import com.graphhopper.ResponsePath;
//import com.graphhopper.util.shapes.GHPoint;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.locationtech.jts.geom.Coordinate;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class RouteChainBuilder {
//
//    private final RouteService routeService;
//    private final RouteEvaluationService routeEvaluationService;
//
//    public RouteChainBuilder(RouteService routeService, RouteEvaluationService routeEvaluationService) {
//        this.routeService = routeService;
//        this.routeEvaluationService = routeEvaluationService;
//    }
//
//    public RouteResultDto buildChainedRoute(Coordinate start, double totalTimeMinutes) {
//        List<GHPoint> chainedPoints = new ArrayList<>();
//        List<ResponsePath> chainedPaths = new ArrayList<>();
//        JSONArray geoJsonFeatures = new JSONArray();
//
//        GHPoint currentPoint = new GHPoint(start.y, start.x);
//        chainedPoints.add(currentPoint);
//
//        double stepTimeLimit = totalTimeMinutes * 0.25;
//        int maxSteps = 4;
//
//        System.out.printf("[START] 총 시간: %.2f분, 실제 사용 시간: %.2f분\n", totalTimeMinutes, stepTimeLimit);
//
//        for (int i = 0; i < maxSteps && stepTimeLimit > 0; i++) {
//            System.out.printf("[STEP %d] 현재 좌표: %.6f, %.6f | 남은 시간: %.2f분\n",
//                    i + 1, currentPoint.getLat(), currentPoint.getLon(), stepTimeLimit);
//
//            RouteResultDto candidateDto = routeService.findBestRoute(currentPoint, (int) stepTimeLimit);
//            if (candidateDto == null || candidateDto.getResponsePath() == null) {
//                System.out.println("[STOP] 경로 없음 또는 ResponsePath null");
//                break;
//            }
//
//            ResponsePath path = candidateDto.getResponsePath();
//            double timeMin = path.getTime() / 60000.0;
//
////            if (timeMin > stepTimeLimit) {
////                System.out.printf("[STOP] 경로 시간 %.2f분 > 남은 시간 %.2f분\n", timeMin, stepTimeLimit);
////                break;
////            }
//
//            GHPoint nextPoint = path.getPoints().get(path.getPoints().size() - 1);
//            System.out.printf("[DEBUG] 다음 지점: %.6f, %.6f\n", nextPoint.getLat(), nextPoint.getLon());
//
//
//
//            if (chainedPoints.size() >= 2) {
//                GHPoint p1 = chainedPoints.get(chainedPoints.size() - 2);
//                GHPoint p2 = chainedPoints.get(chainedPoints.size() - 1);
//                GHPoint p3 = nextPoint;
//                double angle = computeAngle(p1, p2, p3);
//                System.out.printf("[DEBUG] 세 점 각도: %.2f도\n", angle);
//
//                if (angle < 30 || angle > 150) {
//                    System.out.println("[STOP] 각도 조건 불만족");
//                    break;
//                }
//            }
//
//            chainedPoints.add(nextPoint);
//            chainedPaths.add(path);
//            geoJsonFeatures.add(parseGeoJsonFeature(candidateDto.getGeoJson()));
//
//            currentPoint = nextPoint;
//
//            System.out.printf("[CHAIN] 추가 루트: %.2f분\n", timeMin);
//        }
//
//        JSONObject fullGeoJson = new JSONObject();
//        fullGeoJson.put("type", "FeatureCollection");
//        fullGeoJson.put("features", geoJsonFeatures);
//
//        ResponsePath last = chainedPaths.isEmpty() ? null : chainedPaths.get(chainedPaths.size() - 1);
//        System.out.println("[FINISH] 최종 GeoJSON 및 포인트 반환 완료");
//        return new RouteResultDto(fullGeoJson.toString(), chainedPoints, last);
//    }
//
//    private JSONObject parseGeoJsonFeature(String geoJson) {
//        try {
//            org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
//            return (JSONObject) parser.parse(geoJson);
//        } catch (Exception e) {
//            System.out.println("[ERROR] GeoJSON 파싱 실패");
//            e.printStackTrace();
//            return new JSONObject();
//        }
//    }
//
//    private double computeAngle(GHPoint a, GHPoint b, GHPoint c) {
//        double abX = b.lon - a.lon;
//        double abY = b.lat - a.lat;
//        double bcX = c.lon - b.lon;
//        double bcY = c.lat - b.lat;
//
//        double dot = abX * bcX + abY * bcY;
//        double mag1 = Math.sqrt(abX * abX + abY * abY);
//        double mag2 = Math.sqrt(bcX * bcX + bcY * bcY);
//        if (mag1 == 0 || mag2 == 0) return 180.0;
//
//        double cosTheta = dot / (mag1 * mag2);
//        double angleRad = Math.acos(Math.max(-1, Math.min(1, cosTheta)));
//        return Math.toDegrees(angleRad);
//    }
//}