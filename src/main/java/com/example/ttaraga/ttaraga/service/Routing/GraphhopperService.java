package com.example.ttaraga.ttaraga.service.Routing;

/*
GraphHopper Core API 호출 처리
포인트 리스트를 받아 GeoJSON 경로 반환
포인트 리스트 내용 ( 출발지 + 경유지들)

경로요청 - 경로처리 - geojson 변화

여기서 해야하는 일
1. 여러 포인트를 연결하는 경로 요청 (프로필 : bike) 로 설정하기
GraphHoopper 응답을 GeoJson으로 변환하자.
 */


import com.example.ttaraga.ttaraga.dto.Alg2.RouteInfoDto;
import com.example.ttaraga.ttaraga.dto.Alg2.SlopeDto;
import com.example.ttaraga.ttaraga.service.Alg2.CalculateAvrSlope;
import com.example.ttaraga.ttaraga.service.Alg2.WayPointSelectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.geotools.geojson.feature.FeatureJSON;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.graphhopper.GraphHopper;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GraphhopperService {

    private final GraphHopper hopper;
    private final GeometryFactory geometryFactory;
    private final ObjectMapper objectMapper;
    private final CalculateAvrSlope calculateAvrSlope;

    @Autowired
    public GraphhopperService(GraphHopper hopper , CalculateAvrSlope calculateAvrSlope) {
        //bean으로 등록시 에러 없어짐
        this.hopper = hopper;
        this.geometryFactory = new GeometryFactory();
        this.objectMapper = new ObjectMapper();
        //this.wayPointSelectionService = wayPointSelectionService;
        this.calculateAvrSlope = calculateAvrSlope;
    }

    //여러 포인트를 경유하는 경로를 GeoJson으로 변환하기
    public String buildRouteGeoJson(GHPoint start, List<GHPoint> waypoints) {
        GHRequest request = new GHRequest();
        request.setProfile("bike"); //자전거 프로필
        //request.setLocale("ko");

        //출발지 + 경유지 추가
        request.addPoint(start);
        for (GHPoint p : waypoints) {
            request.addPoint(p);
        }

        GHResponse response = hopper.route(request);

        if (response.hasErrors()) {
            throw new RuntimeException("경로 요청 실패 : " + response.getErrors());
        }

        ResponsePath path = response.getBest();
        PointList pointList = path.getPoints();

        //GeoJson 문자열 생성
        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{ \"type\": \"LineString\", \"coordinates\": [");

        for (int i = 0; i < pointList.size(); i++) {
            GHPoint point = pointList.get(i);
            geoJson.append("[")
                    .append(point.lon).append(", ")
                    .append(point.lat).append("]");
            if (i < pointList.size() - 1) {
                geoJson.append(", ");
            }
        }

        geoJson.append("] }");

        String geoJsonString = geoJson.toString(); //완성된 GEOJSON문자열

        //뭘 리턴할지는 나중에 생각하자. 오늘은 틀만 작성하자.
        return geoJsonString;
    }

    //여러 포인트를 경유하는 경로를 GeoJson으로 변환하기
    public String buildRouteGeoJson_ver2(List<GHPoint> waypoints) {
        GHRequest request = new GHRequest();
        request.setProfile("bike"); //자전거 프로필

        //출발지 + 경유지 추가
        for (GHPoint p : waypoints) {
            request.addPoint(p);
        }

        GHResponse response = hopper.route(request);

        ResponsePath path = response.getBest();
        PointList pointList = path.getPoints();

        double distance = path.getDistance()/1000;
        double duration = path.getTime()/60;


        SlopeDto slope = CalculateAvrSlope.calculateAverageSlope(pointList);
        System.out.println("평균 경사: "+slope.getAverageSlope());


        System.out.println("거리: "+distance + " 시간: "+duration);

        //GeoJson 문자열 생성
        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{ \"type\": \"LineString\", \"coordinates\": [");

        for (int i = 0; i < pointList.size(); i++) {
            GHPoint point = pointList.get(i);
            geoJson.append("[")
                    .append(point.lon).append(", ")
                    .append(point.lat).append("]");
            if (i < pointList.size() - 1) {
                geoJson.append(", ");
            }
        }

        geoJson.append("] }");

        String geoJsonString = geoJson.toString(); //완성된 GEOJSON문자열

        //뭘 리턴할지는 나중에 생각하자. 오늘은 틀만 작성하자.
        return geoJsonString;
    }

    // ALG2는 이거 사용함
    //리스트 ( 시작점 , 경유지1, 경유지2, 시작점 ) 내용 담고 있음
    public RouteInfoDto buildRouteGeoJsonWithInfo(List<GHPoint> waypoints) {
        GHRequest request = new GHRequest();
        request.setProfile("bike")
                .putHint("elevation", true); // 경사 설정 열기

        for (GHPoint p : waypoints) {
            request.addPoint(p);
        }

        GHResponse response = hopper.route(request); // 경로 찾기

        if (response.hasErrors()) {
            throw new RuntimeException("경로 계산 실패: " + response.getErrors());
        }

        ResponsePath path = response.getBest();
        PointList pointList = path.getPoints(); // 고도 정보 포함된 3차원 배열 ( 위도 경도 고도 )

        double distance = path.getDistance();        // meter
        double duration = path.getTime() / 60000; // 분

        SlopeDto slope = calculateAvrSlope.calculateAverageSlope(pointList);
        System.out.println("평균 경사: "+slope.getAverageSlope());


        // GeoJSON 생성
        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{ \"type\": \"LineString\", \"coordinates\": [");

        for (int i = 0; i < pointList.size(); i++) {
            GHPoint point = pointList.get(i);
            geoJson.append("[")
                    .append(point.lon).append(", ")
                    .append(point.lat).append("]");
            if (i < pointList.size() - 1) geoJson.append(", ");
        }

        geoJson.append("] }");

        return new RouteInfoDto(geoJson.toString(), distance, duration, slope.getAverageSlope(), slope.getMaxSlope());
    }

    // 단순 경로 요청
    public Optional<ResponsePath> getPath(GHPoint from, GHPoint to) {
        GHRequest request = new GHRequest(from, to).setProfile("bike").setLocale("ko");
        GHResponse response = hopper.route(request);
        return response.hasErrors() ? Optional.empty() : Optional.of(response.getBest());
    }

    public String convertToGeoJson(ResponsePath path) {
        try {
            // 1. 좌표 리스트 → JTS LineString으로 변환
            PointList pointList = path.getPoints();
            Coordinate[] coordinates = new Coordinate[pointList.size()];
            for (int i = 0; i < pointList.size(); i++) {
                coordinates[i] = new Coordinate(pointList.getLon(i), pointList.getLat(i));
            }

            GeometryFactory geometryFactory = new GeometryFactory();
            LineString lineString = geometryFactory.createLineString(coordinates);

            // 2. Feature 타입 정의
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("Route");
            builder.setCRS(null); // 좌표계 지정 안 함 (필요시 setCRS)
            builder.add("the_geom", LineString.class);
            SimpleFeatureType featureType = builder.buildFeatureType();

            // 3. Feature 생성
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
            featureBuilder.add(lineString);
            SimpleFeature feature = featureBuilder.buildFeature(null);

            // 4. GeoJSON 직렬화
            FeatureJSON fjson = new FeatureJSON();
            StringWriter writer = new StringWriter();
            fjson.writeFeature(feature, writer);
            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("GeoJSON 변환 실패", e);
        }
    }

    // geoJson의 대략적인 면적 계산하기
    public double calculateApproximateAreaFromLineString(String geoJsonString) {
        if (geoJsonString == null || geoJsonString.isEmpty()) {
            throw new IllegalArgumentException("GeoJSON string cannot be null or empty.");
        }

        GeoJsonReader reader = new GeoJsonReader(geometryFactory); // GeometryFactory 전달
        try {
            Geometry geometry = reader.read(geoJsonString);

            if (geometry instanceof LineString) {
                LineString lineString = (LineString) geometry;

                // LineString을 LinearRing으로 변환
                // 주의: LineString이 실제로 LinearRing의 유효성 조건을 만족해야 합니다.
                // 예를 들어, 최소 4개의 좌표 (시작점=끝점 포함)를 가져야 합니다.
                // 그렇지 않으면 org.locationtech.jts.geom.TopologyException이 발생할 수 있습니다.
                CoordinateSequence coordinates = lineString.getCoordinateSequence();
                LinearRing linearRing = geometryFactory.createLinearRing(coordinates);

                // LinearRing을 사용하여 Polygon 생성
                // 두 번째 인자(내부 링)는 null로 둡니다.
                Polygon polygon = geometryFactory.createPolygon(linearRing, null);

                // Polygon의 면적 계산
                // 투영된 좌표계에서의 면적을 반환합니다.
                return polygon.getArea();

            } else {
                throw new IllegalArgumentException("Unsupported GeoJSON geometry type. Only LineString is supported for area calculation in this method (assuming it's a closed path).");
            }

        } catch (ParseException e) {
            System.err.println("Error parsing GeoJSON: " + e.getMessage());
            throw new RuntimeException("Failed to parse GeoJSON string.", e);
        } catch (IllegalArgumentException e) {
            System.err.println("GeoJSON validation error: " + e.getMessage());
            throw e; // 호출자에게 전달
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            throw new RuntimeException("An unexpected error occurred during area calculation.", e);
        }

    }


//    public String getRouteGeoJson(GHPoint start, GHPoint end, List<GHPoint> optionalWaypoints) {
//        GHRequest request = new GHRequest();
//        request.setProfile("bike"); // 자전거 프로필 설정
//
//        // 출발지 추가
//        request.addPoint(start);
//
//        // 경유지 추가 (있는 경우)
//        if (optionalWaypoints != null && !optionalWaypoints.isEmpty()) {
//            for (GHPoint p : optionalWaypoints) {
//                request.addPoint(p);
//            }
//        }
//
//        // 도착지 추가
//        request.addPoint(end);
//
//        GHResponse response = hopper.route(request);
//
//        if (response.hasErrors()) {
//            // 에러 로깅 및 예외 처리
//            System.err.println("경로 요청 실패: " + response.getErrors());
//            throw new RuntimeException("경로 요청 실패: " + response.getErrors());
//        }
//
//        ResponsePath path = response.getBest();
//        PointList pointList = path.getPoints();
//
//        // GeoJSON LineString 문자열 생성
//        StringBuilder geoJson = new StringBuilder();
//        geoJson.append("{ \"type\": \"LineString\", \"coordinates\": [");
//
//        for (int i = 0; i < pointList.size(); i++) {
//            GHPoint point = pointList.get(i);
//            // GeoJSON은 [경도, 위도] 순서입니다.
//            geoJson.append("[")
//                    .append(point.lon).append(", ")
//                    .append(point.lat).append("]");
//            if (i < pointList.size() - 1) {
//                geoJson.append(", ");
//            }
//        }
//        geoJson.append("] }");
//
//        return geoJson.toString();
//    }
}