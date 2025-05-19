package com.example.ttaraga.ttaraga.service;

/*
GraphHopper Core API 호출 처리
포인트 리스트를 받아 GeoJSON 경로 반환
포인트 리스트 내용 ( 출발지 + 경유지들)

경로요청 - 경로처리 - geojson 변화

여기서 해야하는 일
1. 여러 포인트를 연결하는 경로 요청 (프로필 : bike) 로 설정하기
GraphHoopper 응답을 GeoJson으로 변환하자.
 */

import com.example.ttaraga.ttaraga.GeoJsonExporter;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.hibernate.query.sqm.tree.domain.SqmPathWrapper;
import org.mariadb.jdbc.type.LineString;
import org.springframework.stereotype.Service;
import com.graphhopper.GraphHopper;

import java.awt.*;
import java.util.List;
import java.util.Locale;

@Service
public class GraphhopperService {

    private final GraphHopper hopper;

    public GraphhopperService(GraphHopper hopper) { //bean으로 등록시 에러 없어짐
        this.hopper = hopper;
    }

    //여러 포인트를 경유하는 경로를 GeoJson으로 변환하기
    public GeoJsonExporter buildRouteGeoJson(GHPoint start, List<GHPoint> waypoints){
        GHRequest request = new GHRequest();
        request.setProfile("bike"); //자전거 프로필
        request.setLocale("ko");

        //출발지 + 경유지 추가
        request.addPoint(start);
        for(GHPoint p : waypoints){
            request.addPoint(p);
        }

        GHResponse response = hopper.route(request);

        if(response.hasErrors()){
            throw new RuntimeException("경로 요청 실패 : "+ response.getErrors());
        }

        ResponsePath path = response.getBest();
        PointList pointList = path.getPoints();

        //GeoJson 문자열 생성
        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{ \"type\": \"LineString\", \"coordinates\": [");

        for(int i=0; i<pointList.size(); i++){
            GHPoint point = pointList.get(i);
            geoJson.append("[")
                    .append(point.lon).append(", ")
                    .append(point.lat).append("]");
            if(i<pointList.size()-1){
                geoJson.append(", ");
            }
        }

        geoJson.append("] }");

        String geoJsonString = geoJson.toString(); //완성된 GEOJSON문자열

        //뭘 리턴할지는 나중에 생각하자. 오늘은 틀만 작성하자.
        return null;
    }


//    GHRequest req = new GHRequest()
//            .addPoint(startPoint)
//            .addPoint(wayPoint1)
//            .addPoint(endPoint)
//            .setProfile("bike")
//            .setLocale(Locale.KOREA);
}
