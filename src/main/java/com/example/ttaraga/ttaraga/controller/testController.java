package com.example.ttaraga.ttaraga.controller;


import com.example.ttaraga.ttaraga.service.GraphhopperService;
import com.graphhopper.util.shapes.GHPoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/route")
public class testController {

    //private final GraphhopperService graphhopperService;

//    public testController(GraphhopperService graphhopperService, GraphhopperService graphhopperService1) {
//        this.graphhopperService = graphhopperService1;
//    }
//
//    // JSON 형태로 start, waypoints를 POST로 받음
//    @PostMapping
//    public Map<String, Object> getRoute(@RequestBody RouteRequest request) {
//        String geoJson = graphhopperService.buildRouteGeoJson(request.getStart(), request.getWaypoints());
//        return Map.of("geojson", geoJson);
//    }
//
//    // 요청 바디로 받을 클래스
//    public static class RouteRequest {
//        private GHPoint start;
//        private List<GHPoint> waypoints;
//
//        public GHPoint getStart() {
//            return start;
//        }
//
//        public void setStart(GHPoint start) {
//            this.start = start;
//        }
//
//        public List<GHPoint> getWaypoints() {
//            return waypoints;
//        }
//
//        public void setWaypoints(List<GHPoint> waypoints) {
//            this.waypoints = waypoints;
//        }
//    }


}
