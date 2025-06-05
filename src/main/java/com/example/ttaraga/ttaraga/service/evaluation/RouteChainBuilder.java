package com.example.ttaraga.ttaraga.service.evaluation;

import com.example.ttaraga.ttaraga.dto.Alg2.RouteInfoDto;
import com.example.ttaraga.ttaraga.dto.Alg2.SlopeDto;
import com.example.ttaraga.ttaraga.dto.BestRouteResultDto;
import com.example.ttaraga.ttaraga.dto.RouteResultDtoTemp;
import com.example.ttaraga.ttaraga.service.Alg2.CalculateAvrSlope;
import com.example.ttaraga.ttaraga.service.Routing.GraphhopperService;
import com.example.ttaraga.ttaraga.service.Routing.RouteService;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class RouteChainBuilder {

    private final RouteService routeService;
    private final RouteEvaluationService routeEvaluationService;
    @Autowired
    private GraphhopperService graphhopperService;

    public RouteChainBuilder(RouteService routeService, RouteEvaluationService routeEvaluationService) {
        this.routeService = routeService;
        this.routeEvaluationService = routeEvaluationService;
    }

    public BestRouteResultDto buildChainedRoute(Coordinate start, double totalTimeMinutes) {
        List<GHPoint> chainedPoints = new ArrayList<>();
        List<ResponsePath> chainedPaths = new ArrayList<>();
        JSONArray geoJsonFeatures = new JSONArray();

        GHPoint p0 = new GHPoint(start.y, start.x);
        chainedPoints.add(p0);
        GHPoint currentPoint = p0;

        double stepTimeLimit = totalTimeMinutes * 0.3;

        System.out.printf("[START] 총 시간: %.2f분, 스텝당 시간 제한: %.2f분\n", totalTimeMinutes, stepTimeLimit);

        for (int i = 0; i < 2; i++) {
            System.out.printf("[STEP %d] 현재 좌표: %.6f, %.6f\n", i + 1, currentPoint.getLat(), currentPoint.getLon());

            List<CandidateRoute> candidates = routeService.findCandidateRoutes(currentPoint, (int) stepTimeLimit);
            candidates.sort(Comparator.comparingDouble(CandidateRoute::getScore).reversed());

            boolean found = false;

            for (CandidateRoute candidate : candidates) {
                GHPoint nextPoint = candidate.getPoint();

                // 각도 조건 검사
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

                // 경사도 검사
                PointList pointList = candidate.getPoints();
                if (pointList == null) {
                    System.out.println("[SKIP] candidate.getPoints()가 null입니다. 후보 경로 무시.");
                    continue;
                }

                List<GHPoint> ghPoints = new ArrayList<>();
                for (int idx = 0; idx < pointList.size(); idx++) {
                    ghPoints.add(pointList.get(idx));
                }

                RouteInfoDto routeInfo = graphhopperService.buildRouteGeoJsonWithInfo(ghPoints);
                if (routeInfo == null) {
                    System.out.println("[SKIP] routeInfo가 null입니다. 경로 생성 실패.");
                    continue;
                }

                if (routeInfo.getMaxSlope() >= 0.08) {
                    System.out.printf("[SKIP] 경사도 조건 불만족 (%.2f%%)\n", routeInfo.getMaxSlope() * 100);
                    continue;
                }

                // ✅ 경로 채택
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

        if (chainedPoints.size() >= 3) {
            GHPoint p1 = chainedPoints.get(1);
            GHPoint p2 = chainedPoints.get(2);

            List<CandidateRoute> p3Candidates = routeService.findCandidateRoutes(p2, (int) stepTimeLimit);
            p3Candidates.sort(Comparator.comparingDouble(CandidateRoute::getScore).reversed());

            GHPoint p3 = null;

            for (CandidateRoute candidate : p3Candidates) {
                GHPoint p3Candidate = candidate.getPoint();

                // 조건 A: p0-p2-p3 각도 (30° 이상 90° 이하)
                double angleA = computeAngle(p0, p2, p3Candidate);
                if (angleA < 30 || angleA > 90) {
                    System.out.printf("[SKIP] 조건A 실패 - p0-p2-p3 각도 %.2f도\n", angleA);
                    continue;
                }

                // 조건 B: p1-p2-p3 각도 (60° 초과)
                double angleB = computeAngle(p1, p2, p3Candidate);
                if (angleB <= 60) {
                    System.out.printf("[SKIP] 조건B 실패 - p1-p2-p3 각도 %.2f도\n", angleB);
                    continue;
                }

                // 경사도 조건
                PointList ptList = candidate.getPoints();
                if (ptList == null) continue;

                List<GHPoint> ghPoints = new ArrayList<>();
                for (int i = 0; i < ptList.size(); i++) {
                    ghPoints.add(ptList.get(i));
                }

                RouteInfoDto routeInfo = graphhopperService.buildRouteGeoJsonWithInfo(ghPoints);
                if (routeInfo == null || routeInfo.getMaxSlope() >= 0.08) {
                    System.out.printf("[SKIP] 경사도 %.2f%% 초과\n", routeInfo != null ? routeInfo.getMaxSlope() * 100 : -1);
                    continue;
                }

                // ✅ 조건 통과한 경우
                p3 = p3Candidate;
                chainedPoints.add(p3);
                chainedPaths.add(candidate.getPath());
                geoJsonFeatures.add(parseGeoJsonFeature(candidate.getGeoJson()));
                System.out.printf("[SELECTED] p3 좌표: %.6f, %.6f (각도A: %.2f, 각도B: %.2f)\n",
                        p3.getLat(), p3.getLon(), angleA, angleB);
                break;
            }

            if (p3 != null && !chainedPoints.contains(p3)) {
                chainedPoints.add(p3);
                RouteResultDtoTemp returnSegment = routeService.findBestRouteBetween(p3, p0);
                if (returnSegment != null && returnSegment.getResponsePath() != null) {
                    chainedPaths.add(returnSegment.getResponsePath());
                    geoJsonFeatures.add(parseGeoJsonFeature(returnSegment.getGeoJson()));
                }
            } else {
                // 🔁 fallback: p3 없으면 마지막 지점에서 복귀
                GHPoint last = chainedPoints.get(chainedPoints.size() - 1);
                if (!last.equals(p0)) {
                    RouteResultDtoTemp fallbackReturn = routeService.findBestRouteBetween(last, p0);
                    if (fallbackReturn != null && fallbackReturn.getResponsePath() != null) {
                        chainedPaths.add(fallbackReturn.getResponsePath());
                        geoJsonFeatures.add(parseGeoJsonFeature(fallbackReturn.getGeoJson()));
                    }
                }
            }
        } else {
            GHPoint lastPoint = chainedPoints.get(chainedPoints.size() - 1);
            if (!lastPoint.equals(p0)) {
                RouteResultDtoTemp returnSegment = routeService.findBestRouteBetween(lastPoint, p0);
                if (returnSegment != null && returnSegment.getResponsePath() != null) {
                    chainedPaths.add(returnSegment.getResponsePath());
                    geoJsonFeatures.add(parseGeoJsonFeature(returnSegment.getGeoJson()));
                }
            }
        }

        JSONObject fullGeoJson = new JSONObject();
        fullGeoJson.put("type", "FeatureCollection");
        fullGeoJson.put("features", geoJsonFeatures);
        String combinedGeoJson = fullGeoJson.toString();
        String mergedGeoJson = mergeGeoJsonCoordinates(combinedGeoJson);

        double totalDistance = 0.0;
        double totalDuration = 0.0;
        double totalSlope = 0.0;
        double maxSlope = 0.0;
        int slopeCount = 0;

//        for (ResponsePath path : chainedPaths) {
//            totalDistance += path.getDistance();
//            totalDuration += path.getTime() / 60000.0 * 1.5;
//
//            // 경사도 계산
//            PointList pointList = path.getPoints();
//            SlopeDto slope = CalculateAvrSlope.calculateAverageSlope(pointList);
//            totalSlope += slope.getAverageSlope();
//            if (slope.getMaxSlope() > maxSlope) {
//                maxSlope = slope.getMaxSlope();
//            }
//            slopeCount++;
//        }
        for (int i = 0; i < chainedPaths.size(); i++) {
            ResponsePath path = chainedPaths.get(i);
            totalDistance += path.getDistance();
            double segmentDuration = path.getTime() / 60000.0;
            totalDuration += segmentDuration;

            // 경사도 계산
            PointList pointList = path.getPoints();
            SlopeDto slope = CalculateAvrSlope.calculateAverageSlope(pointList);
            totalSlope += slope.getAverageSlope();
            if (slope.getMaxSlope() > maxSlope) {
                maxSlope = slope.getMaxSlope();
            }
            slopeCount++;

            // 🪵 디버깅 로그: 경로 구간별 시간 및 좌표 출력
            System.out.printf("🔹 [Segment %d] 거리: %.2fm | 시간: %.2f분 | 경사(평균/최대): %.3f / %.3f\n",
                    i + 1, path.getDistance(), segmentDuration,
                    slope.getAverageSlope(), slope.getMaxSlope());

            for (int j = 0; j < pointList.size(); j++) {
                GHPoint pt = pointList.get(j);
                System.out.printf("    → %.6f, %.6f\n", pt.getLat(), pt.getLon());
            }
        }

        double averageSlope = slopeCount > 0 ? totalSlope / slopeCount : 0.0;

        System.out.printf("[FINISH] 거리: %.2f m, 시간: %.2f 분, 평균 경사: %.2f, 최대 경사: %.2f\n",
                totalDistance, totalDuration, averageSlope, maxSlope);

        return new BestRouteResultDto(mergedGeoJson, totalDistance, totalDuration, averageSlope, maxSlope);
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