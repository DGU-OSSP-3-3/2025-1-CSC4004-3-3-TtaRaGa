package com.example.ttaraga.ttaraga.controller;


import com.example.ttaraga.ttaraga.dto.RouteRequest;
import com.example.ttaraga.ttaraga.service.Routing.GraphhopperService;
import com.graphhopper.util.shapes.GHPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/*
    Controller관련 어노테이션

    @RestController
    Contoller + ResponseBody -> 매서드가 반환하는 객체를 ResponseBody로 직접 변환하여 클라이언트에 전송
    데이터를 반환하는 REST API 서버를 만들 때 사용

    @RequestMapping
    특정 HTTP 요청(URL, method)를 핸들러 메서드에 매핑하는데 ㅅ용

    @GetMapping (HTTP GET 요청 매핑)  -  단순 정보 조회용
    서버로부터 데이터를 조회하거나 가져올 때 사용
    우리의 경우엔 따릉이 아이디, 위치(위도경도) 정보 보내기
    특징
    1. 멱등성 : 같은 요청 여러 번 보내도 서버 상태 변경 x, db에 변화 x
    2. 안정성 : 서버의 data 변경 x
    3. 데이터 전달 : 쿼리 파라미터를 통해 전달. URL뒤에 ?key1=value1&key2=value2 형태
    4. URL에 정보 노출



    @PostMapping (HTTP POST 요청 매핑)
    주로 서버에 새로운 데이터를 생성하거나 제출할 때 사용
    예 : 새로운 게시물 작성, 회원가입, 파일 업로드
    우리의 경우에는 시작점을 바탕으로 GeoJSON 연산
    특징
    1. 비멱등성 : 같은 요청 여러번보내면 서버 상태 여러번 변경 (ex. 게시물 여러개 생성)
    2. 비아정성 : 서버의 data 변경
    3. 데이터 전달 : 주로 요청 본문(Request Body)를 통해 데이터 전달 (JSON XML 등등)
    4. URL에 노출 x


     */
@RestController
@RequestMapping("/api/route")
public class RoutingController {

    private final GraphhopperService graphhopperService;

    // 생성자 주입 : GraphhopperService 빈 자동으로 주입
    //솔직히 빈(Bean)이 뭔지 이해못함
    public RoutingController(GraphhopperService graphhopperService) {
        this.graphhopperService = graphhopperService;
    }

    /**
     * 임의의 출발점, 경유지, 도착점을 사용하여 경로를 계산하고 GeoJSON으로 반환하는 예제 엔드포인트.
     * 이 메서드는 테스트를 위해 하드코딩된 좌표를 사용합니다.
     */


    //테스트용으로 고정값 넣어서 프론트에 쏴봄
    // HTTP GET 요청을 "/api/route/test-bike-route"로 매핑
    @GetMapping("/test-bike-route")
    public ResponseEntity<String> getTestBikeRoute() {
        // --- 임의의 출발점, 경유지, 도착점 설정 ---
        // 출발: 서울시청 부근 임의로 지정한거임
        //나중에 프론트에서 따릉이 이름 쏴주면 db에서 위도 가져오기
        GHPoint startPoint = new GHPoint(37.5665, 126.9780);

        // 경유지: 광화문광장 부근 (임의의 위치)
        //나중에 포인트고르는 알고리즘을 통해 waypoints 리턴 해주기
        List<GHPoint> waypoints = new ArrayList<>();
        waypoints.add(new GHPoint(37.5750, 126.9770)); // 광화문광장 대략적인 위도, 경도

        // 도착: 남산타워 부근
        GHPoint endPoint = new GHPoint(37.5512, 126.9882);

        try {
            String geoJsonRoute = graphhopperService.getRouteGeoJson(startPoint, endPoint, waypoints);
            return ResponseEntity.ok(geoJsonRoute); // 200 OK와 함께 GeoJSON 반환
        } catch (RuntimeException e) {
            System.err.println("경로 계산 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"경로 계산 실패: " + e.getMessage() + "\"}");
        }
    }

    /**
     * 프론트엔드에서 요청받은 출발점, 도착점, 경유지를 바탕으로 경로를 계산하여 GeoJSON으로 반환하는 엔드포인트.
     * HTTP POST 요청으로 RouteRequest DTO를 받습니다.
     * @param request 출발/도착/경유지 정보를 담은 DTO
     * @return GeoJSON LineString 형태의 경로 데이터
     */

    //실제 사용할 코드
    //프론트에서 시작점을 받으면 계산해서 GeoJson반환해줌
    // HTTP POST 요청을 "/api/route/calculate"로 매핑
    @PostMapping("/calculate")
    public ResponseEntity<String> calculateRoute(@RequestBody RouteRequest request) {
        // 요청 DTO의 출발점이나 도착점이 null이면 400 Bad Request 반환
        if (request.getStart() == null || request.getEnd() == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"출발점과 도착점은 필수입니다.\"}");
        }

        try {
            // GraphhopperService를 호출하여 경로 계산
            // 경유지가 없으면 null 또는 빈 리스트가 서비스로 전달됩니다.
            String geoJsonRoute = graphhopperService.getRouteGeoJson(
                    request.getStart(),
                    request.getEnd(),
                    request.getWaypoints() // 경유지가 없으면 null 또는 빈 리스트 전달
            );
            return ResponseEntity.ok(geoJsonRoute);
        } catch (RuntimeException e) {
            System.err.println("경로 계산 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"경로 계산 실패: " + e.getMessage() + "\"}");
        }
    }
}
