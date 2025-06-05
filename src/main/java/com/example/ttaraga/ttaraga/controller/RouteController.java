package com.example.ttaraga.ttaraga.controller;

import com.example.ttaraga.ttaraga.dto.RouteSearchRequestDto;
import com.example.ttaraga.ttaraga.service.Alg2.WayPointSelectionService;
import com.example.ttaraga.ttaraga.service.evaluation.MidpointCalculatorService;
import com.example.ttaraga.ttaraga.service.Routing.GraphhopperService;
import com.example.ttaraga.ttaraga.service.Routing.RouteService;
import com.example.ttaraga.ttaraga.service.evaluation.RouteChainBuilder;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import org.locationtech.jts.geom.Coordinate;
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

    private final RouteService routeService;
    private final MidpointCalculatorService midpointService;
    private final GraphhopperService graphhopperService;
    private final RouteChainBuilder routeChainBuilder;
    private final WayPointSelectionService wayPointSelectionService;


    @Autowired
    public RouteController(RouteService routeService, MidpointCalculatorService midpointService, GraphhopperService graphhopperService,
                           RouteChainBuilder routeChainBuilder) {
        this.routeService = routeService;
        this.midpointService = midpointService;
        this.graphhopperService = graphhopperService;
        this.routeChainBuilder = routeChainBuilder;
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
    @PostMapping("/best/single")
    public RouteResultDto bestRouteSingle(@RequestBody RouteRequestDto request) {
        return routeService.findBestRoute(request.getStart(), request.getTimeLimitMinutes());
    }

    @PostMapping("/best")
    public RouteResultDto bestRoute(@RequestBody RouteRequestDto request) {
        Coordinate start = new Coordinate(request.getLon(), request.getLat());
        double timeLimit = request.getTimeLimitMinutes();

        return routeChainBuilder.buildChainedRoute(start, timeLimit);
    }

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


      