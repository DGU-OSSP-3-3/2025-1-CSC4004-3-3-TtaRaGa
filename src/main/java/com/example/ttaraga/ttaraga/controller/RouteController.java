package com.example.ttaraga.ttaraga.controller;

import com.example.ttaraga.ttaraga.dto.RouteRequestDto;
import com.example.ttaraga.ttaraga.dto.RouteResultDto;
import com.example.ttaraga.ttaraga.service.MidpointCalculatorService;
import com.example.ttaraga.ttaraga.service.RouteService;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
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

    public RouteController(RouteService routeService, MidpointCalculatorService midpointService) {
        this.routeService = routeService;
        this.midpointService = midpointService;
    }

    /**
     * ✅ 목적: 출발지 기준 일정 시간 내 갈 수 있는 가장 점수 높은 지점 경로 추천
     * 🔗 POST /api/route/best
     */
    @PostMapping("/best")
    public RouteResultDto bestRoute(@RequestBody RouteRequestDto request) {
        return routeService.findBestRoute(request.getStart(), request.getTimeLimitMinutes());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public class CustomNotFoundException extends RuntimeException {
        public CustomNotFoundException(String message) {
            super(message);
        }
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
     * 🧪 테스트용: 일정 시간 기준 도달 가능한 거리 내 중간 지점 계산 + 경로 정보 반환
     * 🔗 GET /api/route/recommend?lat=...&lon=...&time=...
     */
    @GetMapping("/recommend")
    public Map<String, Object> recommendPath(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double time // 단위: 분
    ) {
        GHPoint start = new GHPoint(lat, lon);
        double timeInHours = time / 60.0;

        GHPoint mid = midpointService.findMidpoint(start, timeInHours);
        ResponsePath path = midpointService.getPath(start, mid);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("start", start);
        result.put("midpoint", mid);
        result.put("distance_km", path.getDistance() / 1000.0);
        result.put("time_minutes", path.getTime() / 60000.0);
        result.put("points", path.getPoints().toLineString(false)); // 단순한 좌표 배열

        return result;
    }
}