package com.example.ttaraga.ttaraga.service.evaluation;

import com.example.ttaraga.ttaraga.dto.BestRouteResultDto;
import com.example.ttaraga.ttaraga.dto.RouteResultDtoTemp;
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
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class RouteChainBuilder {

    private final RouteService routeService;
    private final RouteEvaluationService routeEvaluationService;

    public RouteChainBuilder(RouteService routeService, RouteEvaluationService routeEvaluationService) {
        this.routeService = routeService;
        this.routeEvaluationService = routeEvaluationService;
    }

    public RouteResultDtoTemp buildChainedRoute(Coordinate start, double totalTimeMinutes) {
        List<GHPoint> chainedPoints = new ArrayList<>();
        List<ResponsePath> chainedPaths = new ArrayList<>();
        JSONArray geoJsonFeatures = new JSONArray();

        GHPoint p0 = new GHPoint(start.y, start.x);
        chainedPoints.add(p0);
        GHPoint currentPoint = p0;

        double stepTimeLimit = totalTimeMinutes * 0.25;

        System.out.printf("[START] 총 시간: %.2f분, 스텝당 시간 제한: %.2f분\n", totalTimeMinutes, stepTimeLimit);

        for (int i = 0; i < 2; i++) {  // 최대 2개 경유지 생성
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

        // 🔁 p0, p1, p2가 있다면 대칭 경유지 추가 및 복귀 루트 설정
        if (chainedPoints.size() >= 3) {
            GHPoint p1 = chainedPoints.get(1);
            GHPoint p2 = chainedPoints.get(2);
            GHPoint p3 = reflectPoint(p0, p2, p1);  // 대칭 경유지

            if (!chainedPoints.contains(p3)) {
                chainedPoints.add(p3);
            }

            // p2 → p3
            RouteResultDtoTemp segment1 = routeService.findBestRouteBetween(p2, p3);
            if (segment1 != null && segment1.getResponsePath() != null) {
                chainedPaths.add(segment1.getResponsePath());
                geoJsonFeatures.add(parseGeoJsonFeature(segment1.getGeoJson()));
            }

            // p3 → p0 (복귀)
            RouteResultDtoTemp segment2 = routeService.findBestRouteBetween(p3, p0);
            if (segment2 != null && segment2.getResponsePath() != null) {
                chainedPaths.add(segment2.getResponsePath());
                geoJsonFeatures.add(parseGeoJsonFeature(segment2.getGeoJson()));
            }
        } else {
            // 최소 경유지가 부족해도 반드시 p0로 귀환
            GHPoint lastPoint = chainedPoints.get(chainedPoints.size() - 1);
            if (!lastPoint.equals(p0)) {
                RouteResultDtoTemp returnSegment = routeService.findBestRouteBetween(lastPoint, p0);
                if (returnSegment != null && returnSegment.getResponsePath() != null) {
                    chainedPaths.add(returnSegment.getResponsePath());
                    geoJsonFeatures.add(parseGeoJsonFeature(returnSegment.getGeoJson()));
                }
            }
        }

        // GeoJSON 병합
        JSONObject fullGeoJson = new JSONObject();
        fullGeoJson.put("type", "FeatureCollection");
        fullGeoJson.put("features", geoJsonFeatures);
        String combinedGeoJson = fullGeoJson.toString();

        String mergedGeoJson = mergeGeoJsonCoordinates(combinedGeoJson);
        ResponsePath last = chainedPaths.isEmpty() ? null : chainedPaths.get(chainedPaths.size() - 1);

        System.out.println("[FINISH] 최종 GeoJSON 및 포인트 반환 완료");

        double totalDistance = 0.0;
        double totalDuration = 0.0;

        for (ResponsePath path : chainedPaths) {
            totalDistance += path.getDistance(); // m
            totalDuration += path.getTime() / 1000.0; // ms → s
        }

        System.out.println("[Debug] distance: " + totalDistance + "m, duration: " + totalDuration + "s");


        // ✅ 중복 제거 후 반환
        List<GHPoint> dedupedPoints = new ArrayList<>(new LinkedHashSet<>(chainedPoints));
        return new RouteResultDtoTemp(mergedGeoJson, dedupedPoints, last);
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
                JSONObject geometry = (JSONObject) feature.get("geometry");
                JSONArray coords = (JSONArray) geometry.get("coordinates");
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

    private GHPoint reflectPoint(GHPoint base1, GHPoint base2, GHPoint toReflect) {
        // base1과 base2를 잇는 선분을 기준으로 toReflect를 대칭
        double dx = base2.lon - base1.lon;
        double dy = base2.lat - base1.lat;

        double a = dy;
        double b = -dx;
        double c = dx * base1.lat - dy * base1.lon;

        double d = (a * toReflect.lon + b * toReflect.lat + c) / (a * a + b * b);

        double xPrime = toReflect.lon - 2 * a * d;
        double yPrime = toReflect.lat - 2 * b * d;

        return new GHPoint(yPrime, xPrime);
    }

    private GHPoint symmetricPoint(GHPoint center, GHPoint original) {
        // center를 기준으로 original의 대칭점
        double lat = 2 * center.lat - original.lat;
        double lon = 2 * center.lon - original.lon;
        return new GHPoint(lat, lon);
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