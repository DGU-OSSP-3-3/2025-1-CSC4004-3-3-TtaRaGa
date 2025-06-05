package com.example.ttaraga.ttaraga.service.Alg2;

import com.example.ttaraga.ttaraga.dto.Alg2.RouteCandidateDto;
import com.example.ttaraga.ttaraga.dto.Alg2.RouteInfoDto;
import com.example.ttaraga.ttaraga.dto.Alg2.RouteResultDto;

import com.example.ttaraga.ttaraga.repository.AttractingRepository;

import com.example.ttaraga.ttaraga.service.Routing.GraphhopperService;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.shapes.GHPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.graphhopper.ResponsePath;

import java.util.ArrayList;
import java.util.List;



@Service
public class WayPointSelectionService {

    private final AttractingRepository attractingPlaceRepository;
    private final GraphHopper hopper;
    private final GraphhopperService graphhopperService;


    @Autowired
    public WayPointSelectionService(AttractingRepository attractingPlaceRepository,GraphHopper hopper, GraphhopperService graphhopperService) {

        this.attractingPlaceRepository = attractingPlaceRepository;
        this.hopper = hopper;
        this.graphhopperService = graphhopperService;

    }

    /**
     * 사용자의 요청 조건에 따라 최적의 경유지 두 개를 선정하여 GHPoint 리스트로 반환합니다.
     * 이 메서드는 AttractingPlaceRepository의 복잡한 SQL 쿼리 로직을 호출하여
     * 조건에 맞는 두 개의 랜드마크(place1, place2)를 찾아 GHPoint로 변환합니다.
     *
     * @param startLat           출발점 위도
     * @param startLon           출발점 경도
     * @param minRouteDistance   경로 추천을 위한 최소 총 거리 (DB 쿼리 파라미터)
     * @param maxRouteDistance   경로 추천을 위한 최대 총 거리 (DB 쿼리 파라미터)
     * @param candidateLimit     DB에서 조회할 후보군 수 (최적의 1개만 찾으려면 1로 설정)
     * @return 선정된 두 개의 경유지 (GHPoint) 리스트, 또는 빈 리스트 (적합한 경로 없을 시)
     */
    public RouteResultDto generateBestRoute(
            double startLat, double startLon,
            double minRouteDistance, double maxRouteDistance,
            int candidateLimit, double maxAllowedDurationMinutes ) {

        double maxArea = -1.0;
        RouteResultDto bestResult = null;

        GHPoint startPoint = new GHPoint(startLat, startLon);

        List<RouteCandidateDto> candidates = attractingPlaceRepository.findRouteCandidates(
                startLon, startLat, minRouteDistance, maxRouteDistance, candidateLimit
        );

        for (RouteCandidateDto candidate : candidates) {
            GHPoint point1 = new GHPoint(candidate.getPoint1_lat(), candidate.getPoint1_lon());
            GHPoint point2 = new GHPoint(candidate.getPoint2_lat(), candidate.getPoint2_lon());

            List<GHPoint> route = List.of(startPoint, point1, point2, startPoint);

            try {
                RouteInfoDto info = graphhopperService.buildRouteGeoJsonWithInfo(route); // 거리, 시간 포함된 메서드 사용
                double durationMinutes = info.getDuration();

                if (durationMinutes >= maxAllowedDurationMinutes) {
                    System.out.println("제한 시간 초과 경로 건너뜀 (" + durationMinutes + "분 > " + maxAllowedDurationMinutes + "분)");
                    continue;
                }

                double area = graphhopperService.calculateApproximateAreaFromLineString(info.getGeoJson());

                // 면적이 가장 넓으면 bestResult 갱신
                if (area > maxArea) {

                    maxArea = area;
                    bestResult = new RouteResultDto(
                            candidate.getPoint1_name(),
                            candidate.getPoint2_name(),
                            info.getGeoJson(),
                            info.getDistance()/1000,
                            info.getDuration(),
                            info.getAverageSlope()*100,
                            info.getMaxSlope()*100
                    );
                }
            } catch (Exception e) {
                System.err.println("실패 (" + candidate.getPoint1_name() + ", " + candidate.getPoint2_name() + "): " + e.getMessage());
            }
        }

        return bestResult;
    }



//    //후보 하나 반환하기
//    public List<GHPoint> selectOptimalWaypoints(
//            double startLat, double startLon,
//            double minRouteDistance, double maxRouteDistance,
//            int candidateLimit) {
//
//        // AttractingPlaceRepository에서 정의한 복잡한 SQL 쿼리를 호출합니다.
//        // 이 쿼리는 총 거리, 각도, 예각 삼각형 조건 등을 모두 만족하는
//        // 최적의 '후보 지점 쌍'을 찾아 RouteCandidateDto 형태로 반환합니다.
//        List<RouteCandidateDto> candidates = attractingPlaceRepository.findRouteCandidates(
//                startLon, startLat, minRouteDistance, maxRouteDistance, candidateLimit
//        );
//
//        List<GHPoint> selectedWaypoints = new ArrayList<>();
//
//        // 여기서는 가장 먼저 찾아진 후보 (즉, SQL ORDER BY와 LIMIT에 의해 결정된 최상위 1개)를 선택합니다.
//        // 만약 candidateLimit이 10이라면, 이 리스트는 최대 10개의 후보를 포함할 수 있지만,
//        // 이 서비스의 목적이 "두 점"을 반환하는 것이라면, 첫 번째 후보만 사용합니다.
//        if (!candidates.isEmpty()) {
//            RouteCandidateDto bestCandidate = candidates.get(0); // 첫 번째(최적) 후보 선택
//            selectedWaypoints.add(new GHPoint(bestCandidate.getPoint1_lat(), bestCandidate.getPoint1_lon()));
//            selectedWaypoints.add(new GHPoint(bestCandidate.getPoint2_lat(), bestCandidate.getPoint2_lon()));
//        }
//
//        return selectedWaypoints;
//    }

//
//    //후보 10개 반환하기 -> 이걸 geoJson에 돌려서 판별하기
//    public List<List<GHPoint>> selectOptimalWaypoints_ver2(
//            double startLat, double startLon,
//            double minRouteDistance, double maxRouteDistance,
//            int candidateLimit
//    ){
//        List<RouteCandidateDto> candidates = attractingPlaceRepository.findRouteCandidates(
//                startLon,startLat, minRouteDistance, maxRouteDistance, candidateLimit
//        );
//        // 각 경로 후보(point1, point2)를 독립적인 리스트로 담을 최종 결과 리스트
//        List<List<GHPoint>> allRouteCandidates = new ArrayList<>();
//
//        for (RouteCandidateDto candidate : candidates) {
//            List<GHPoint> routePoints = new ArrayList<>();
//            // 첫 번째 경유지 추가
//            routePoints.add(new GHPoint(candidate.getPoint1_lat(), candidate.getPoint1_lon()));
//            // 두 번째 경유지 추가
//            routePoints.add(new GHPoint(candidate.getPoint2_lat(), candidate.getPoint2_lon()));
//
//            allRouteCandidates.add(routePoints); // 완성된 경로 후보 쌍을 최종 리스트에 추가
//        }
//
//        System.out.println(allRouteCandidates);
//
//        return allRouteCandidates;
//
//    }


//    public String generateRouteGeoJSONsForCandidates(
//            double startLat, double startLon,
//            double minRouteDistance, double maxRouteDistance,
//            int candidateLimit) {
//
//        String bestRouteGeoJSON = "{}"; // 가장 넓은 면적을 가진 경로 GeoJSON (초기값: 빈 객체)
//        double maxArea = -1.0; // 가장 넓은 면적을 추적 (초기값: 유효하지 않은 음수)
//        String currentRouteGeoJson = "";
//        List<RouteCandidateDto> candidates = attractingPlaceRepository.findRouteCandidates(
//                startLon, startLat, minRouteDistance, maxRouteDistance, candidateLimit
//        );
//
//        List<String> routeGeoJSONs = new ArrayList<>();
//        GHPoint startPoint = new GHPoint(startLat, startLon);
//
//        for (RouteCandidateDto candidate : candidates) {
//            GHPoint point1 = new GHPoint(candidate.getPoint1_lat(), candidate.getPoint1_lon());
//            GHPoint point2 = new GHPoint(candidate.getPoint2_lat(), candidate.getPoint2_lon());
//
//            // 순환 경로를 위한 경유지 리스트 생성: [point1, point2, startPoint]
//            List<GHPoint> waypointsForCircularRoute = new ArrayList<>();
//            waypointsForCircularRoute.add(startPoint);
//            waypointsForCircularRoute.add(point1);
//            waypointsForCircularRoute.add(point2);
//            waypointsForCircularRoute.add(startPoint); // 출발점으로 다시 돌아오는 경로를 위해 최종 경유지로 추가
//
//            System.out.println(waypointsForCircularRoute);
//
//            try {
//                // GraphhopperService의 buildRouteGeoJson 메서드 사용
//                //String geoJsonString = graphhopperService.buildRouteGeoJson(startPoint, waypointsForCircularRoute);
//                //routeGeoJSONs.add(geoJsonString);
//                // GraphhopperService의 buildRouteGeoJson 메서드를 사용하여 경로 GeoJSON 생성
//                // 이 GeoJSON은 LineString 형태일 것입니다.
//                currentRouteGeoJson = graphhopperService.buildRouteGeoJson_ver2(waypointsForCircularRoute);
//
//                // GeoJSON 면적 계산 (LineString이 닫힌 형태로 가정)
//                // GeoJsonAreaCalculatorService의 calculateApproximateAreaFromLineString 사용
//                double currentArea = graphhopperService.calculateApproximateAreaFromLineString(currentRouteGeoJson);
//
//
//
//                // 현재 경로의 면적이 지금까지 찾은 최대 면적보다 크다면 업데이트
//                if (currentArea > maxArea) {
//                    maxArea = currentArea;
//                    bestRouteGeoJSON = currentRouteGeoJson;
//                }
//            } catch (RuntimeException e) {
//                // 경로 요청 실패 시 처리 (예: 로그, null 추가 등)
//                System.err.println("경로 GeoJSON 생성 실패 (후보: " + candidate.getPoint1_name() + ", " + candidate.getPoint2_name() + "): " + e.getMessage());
//                // 필요하다면 빈 문자열이나 "error" 표시를 추가할 수 있습니다.
//                routeGeoJSONs.add("{}"); // 예시로 빈 GeoJSON 객체 추가
//            }
//        }
//
//        System.out.println(bestRouteGeoJSON);
//        return bestRouteGeoJSON;
//    }




}