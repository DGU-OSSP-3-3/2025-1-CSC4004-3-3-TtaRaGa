package com.example.ttaraga.ttaraga.controller;

import com.example.ttaraga.ttaraga.dto.RouteSearchRequestDto;
import com.example.ttaraga.ttaraga.service.Alg2.WayPointSelectionService;
import com.example.ttaraga.ttaraga.service.evaluation.MidpointCalculatorService;
import com.example.ttaraga.ttaraga.service.Routing.GraphhopperService;
import com.example.ttaraga.ttaraga.service.Routing.RouteService;
import com.example.ttaraga.ttaraga.dto.Alg2.RouteResultDto;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/route")
@Validated
public class RouteController {

//    private final RouteService routeService;
//    private final MidpointCalculatorService midpointService;
//    private final GraphhopperService graphhopperService;
    private final WayPointSelectionService wayPointSelectionService;

    @Autowired
    public RouteController( WayPointSelectionService wayPointSelectionService) {
//        this.routeService = routeService;
//        this.midpointService = midpointService;
//        this.graphhopperService = graphhopperService;
        this.wayPointSelectionService = wayPointSelectionService;
    }

    //최종 본 ALG2
    @PostMapping("/bike-route-time")
    public ResponseEntity<RouteResultDto> getBikeRouteWithTime(@RequestBody Map<String, Object> params){
        double startLat = ((Number) params.getOrDefault("startLat", 37.55274582)).doubleValue();
        double startLon = ((Number) params.getOrDefault("startLon", 126.91861725)).doubleValue();
        int targetTime = ((Number) params.getOrDefault("targetTime", 120)).intValue(); // 단위: 분

        //double minDistance = ((Number) params.getOrDefault("minDistance", 15000)).doubleValue();
        //double maxDistance = ((Number) params.getOrDefault("maxDistance", 20000)).doubleValue();
        int limit = ((Number) params.getOrDefault("limit", 30)).intValue();
        double estimatedSpeed = 250; // m/min 기준
        double minDistance = targetTime * estimatedSpeed * 0.8;
        double maxDistance = targetTime * estimatedSpeed * 1.2;


        RouteResultDto bestRoute = wayPointSelectionService.generateBestRoute(
                startLat, startLon, minDistance, maxDistance, limit, targetTime
        );

        return ResponseEntity.ok(bestRoute);
    }

    //이건 geoJSon을 문자열로만 반환 (거리, 시간 정보 x)
//    @PostMapping("/bike-route")
//    public String getBikeRoute(@RequestBody Map<String, Object> params){
//        double startLat = ((Number) params.getOrDefault("startLat", 37.55274582)).doubleValue();
//        double startLon = ((Number) params.getOrDefault("startLon", 126.91861725)).doubleValue();
//        double minDistance = ((Number) params.getOrDefault("minDistance", 15000)).doubleValue();
//        double maxDistance = ((Number) params.getOrDefault("maxDistance", 20000)).doubleValue();
//        int limit = ((Number) params.getOrDefault("limit", 1)).intValue();
//
//        GHPoint startPoint = new GHPoint(startLat, startLon);
//
//        String selectionWaypoints = wayPointSelectionService.generateRouteGeoJSONsForCandidates(
//                startLat, startLon, minDistance, maxDistance, limit
//        );
//
//        return selectionWaypoints;
//    }

    //거리 시간 정보도 같이 보내는 코드
//    @GetMapping("/test-bike-route")
//    public String getTestBikeRoute(
//            @RequestParam(defaultValue = "37.55274582") double startLat, // 컨트롤러에서 출발점 받기
//            @RequestParam(defaultValue = "126.91861725") double startLon,
//            @RequestParam(defaultValue = "15000") double minDistance, // 웨이포인트 선택 조건
//            @RequestParam(defaultValue = "20000") double maxDistance, // 웨이포인트 선택 조건
//            @RequestParam(defaultValue = "1") int limit // 최적의 웨이포인트 1쌍만 가져오도록
//    ) {
//        // --- 임의의 출발점, 경유지, 도착점 설정 ---
//        // 출발: 서울시청 부근 임의로 지정한거임
//        //나중에 프론트에서 따릉이 이름 쏴주면 db에서 위도 가져오기
//        GHPoint startPoint = new GHPoint(startLat, startLon);
//
//        String selectionWaypoints = wayPointSelectionService.generateRouteGeoJSONsForCandidates(
//                startLat, startLon, minDistance, maxDistance,10
//        );
//
//        return selectionWaypoints;
//    }


    /**
     * 🧪 테스트용: 일정 시간 기준 도달 가능한 거리 내 중간 지점 계산 + 경로 정보 반환
     * 🔗 GET /api/route/recommend?lat=...&lon=...&time=...
     */
//    @GetMapping("/recommend")
//    public Map<String, Object> recommendPath(
//            @RequestParam double lat,
//            @RequestParam double lon,
//            @RequestParam double time // 단위: 분
//    ) {
//        GHPoint start = new GHPoint(lat, lon);
//        double timeInHours = time / 60.0;
//
//        GHPoint mid = midpointService.findMidpoint(start, timeInHours);
//        ResponsePath path = midpointService.getPath(start, mid);
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("start", start);
//        result.put("midpoint", mid);
//        result.put("distance_km", path.getDistance() / 1000.0);
//        result.put("time_minutes", path.getTime() / 60000.0);
//        result.put("points", path.getPoints().toLineString(false)); // 단순한 좌표 배열
//        return result;
//    }

    //프론트에서 시작점을 받으면 계산해서 GeoJson반환해줌
//    @PostMapping("/calculate")
//    public ResponseEntity<String> calculateRoute(@RequestBody RouteSearchRequestDto request) {
//        // 요청 DTO의 출발점이나 도착점이 null이면 400 Bad Request 반환
//        if (request.getStart() == null || request.getEnd() == null) {
//            return ResponseEntity.badRequest().body("{\"error\": \"출발점과 도착점은 필수입니다.\"}");
//        }
//        try {
//            // GraphhopperService를 호출하여 경로 계산
//            // 경유지가 없으면 null 또는 빈 리스트가 서비스로 전달됩니다.
//            String geoJsonRoute = graphhopperService.getRouteGeoJson(
//                    request.getStart(),
//                    request.getEnd(),
//                    request.getWaypoints() // 경유지가 없으면 null 또는 빈 리스트 전달
//            );
//            return ResponseEntity.ok(geoJsonRoute);
//        } catch (RuntimeException e) {
//            System.err.println("경로 계산 중 오류 발생: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("{\"error\": \"경로 계산 실패: " + e.getMessage() + "\"}");
//        }
//    }

}


/**
 * 프론트엔드에서 요청받은 출발점, 도착점, 경유지를 바탕으로 경로를 계산하여 GeoJSON으로 반환하는 엔드포인트.
 * HTTP POST 요청으로 RouteRequest DTO를 받습니다.
 * @param request 출발/도착/경유지 정보를 담은 DTO
 * @return GeoJSON LineString 형태의 경로 데이터
 */


//    @PostMapping("/best")
//    public ResponseEntity<?> bestRoute(@RequestBody RouteRequestDto requestDto) {
//        try {
//            RouteResponseDto result = routeService.findBestRoute(requestDto);
//            return ResponseEntity.ok(result);
//        } catch (IllegalStateException e) {
//            return ResponseEntity
//                    .status(HttpStatus.NOT_FOUND)
//                    .body(Map.of("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "서버 오류 발생: " + e.getMessage()));
//        }
//    }

/**
 * 임의의 출발점, 경유지, 도착점을 사용하여 경로를 계산하고 GeoJSON으로 반환하는 예제 엔드포인트.
 * 이 메서드는 테스트를 위해 하드코딩된 좌표를 사용합니다.
 */


//        try{
//            if ("{}".equals(selectionWaypoints) || selectionWaypoints.isEmpty() ) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body("{\"error\": \"경로를 생성할 적합한 경유지를 찾을 수 없습니다.\"}");
//            }
//        }catch (IllegalArgumentException e) {
//            // WayPointSelectionService 내부에서 발생한 유효하지 않은 인자 관련 예외 처리
//            System.err.println("요청 파라미터 또는 내부 로직 오류: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("{\"error\": \"" + e.getMessage() + "\"}");
//        } catch (RuntimeException e) {
//            // 그 외 예기치 않은 내부 오류 처리
//            System.err.println("경로 계산 중 예기치 않은 오류 발생: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("{\"error\": \"경로 계산 실패: " + e.getMessage() + "\"}");
//        }
// 선택된 웨이포인트가 없으면 에러 반환


// 경유지: 광화문광장 부근 (임의의 위치)
//나중에 포인트고르는 알고리즘을 통해 waypoints 리턴 해주기
// WaypointSelectionService를 통해 최적의 웨이포인트 두 개를 가져옵니다.
//List<GHPoint> waypoints = new ArrayList<>();
//        List<GHPoint> selectionWaypoints = wayPointSelectionService.selectOptimalWaypoints(
//                startLat,startLon,minDistance,maxDistance,limit
//        );

// 폐쇄 경로를 위한 전체 포인트 리스트 구성: 시작점 + 경유지1 + 경유지2 + 시작점
//List<GHPoint> fullRoutePoints = new ArrayList<>();
//fullRoutePoints.add(startPoint);
//fullRoutePoints.addAll(selectionWaypoints); // 선택된 2개의 경유지 추가
//fullRoutePoints.add(startPoint); // 폐쇄 경로를 위해 다시 시작점 추

//waypoints.add(new GHPoint(37.5750, 126.9770)); // 광화문광장 대략적인 위도, 경도

// 도착: 남산타워 부근
//GHPoint endPoint = new GHPoint(37.5512, 126.9882);

//        try {
//            String geoJsonRoute = graphhopperService.calculateApproximateAreaFromLineString(startPoint, endPoint, minDistance,maxDistance,);
//            return ResponseEntity.ok(geoJsonRoute); // 200 OK와 함께 GeoJSON 반환
//        } catch (RuntimeException e) {
//            System.err.println("경로 계산 중 오류 발생: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("{\"error\": \"경로 계산 실패: " + e.getMessage() + "\"}");
//        }

/**
 * ✅ 목적: 출발지 기준 일정 시간 내 갈 수 있는 가장 점수 높은 지점 경로 추천
 * 🔗 POST /api/route/best
 */
//    @PostMapping("/best")
//    public RouteResultDto bestRoute(@RequestBody RouteRequestDto request) {
//        return routeService.findBestRoute(request.getStart(), request.getTimeLimitMinutes());
//    }
//
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public class CustomNotFoundException extends RuntimeException {
//        public CustomNotFoundException(String message) {
//            super(message);
//        }
//    }
