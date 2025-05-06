package com.example.ttaraga.ttaraga;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.shapes.GHPoint;

public class RouteFinder {
    public static void main(String[] args) {
        // 1. GraphHopper 초기화
        GraphHopper hopper = new GraphHopper();
        hopper.setGraphHopperLocation("graph-cache");

        // 2. 동일한 profile로 설정 (GraphBuilder와 완전히 동일하게)
        hopper.setProfiles(
                new Profile("bike")
                        .setVehicle("bike")
                        .setWeighting("custom")
                        .setCustomModel(new CustomModel())
        );

        // 3. 기존 graph-cache 로드
        hopper.load();

        // 4. 출발지 - 도착지 설정 (예: 서울시청 → 남산타워)
        GHRequest req = new GHRequest(
                new GHPoint(37.5665, 126.9780), // start: 서울시청
                new GHPoint(37.5512, 126.9882)  // end: 남산서울타워
        ).setProfile("bike");

        GHResponse res = hopper.route(req);

        // 5. 오류 체크
        if (res.hasErrors()) {
            System.out.println("에러 발생: " + res.getErrors());
            return;
        }

        // 6. 경로 결과 출력
        var path = res.getBest();
        System.out.println("총 거리: " + path.getDistance() + " m");
        System.out.println("예상 시간: " + path.getTime() / 1000.0 + " 초");
        System.out.println("경로 포인트 수: " + path.getPoints().size());

        GeoJsonExporter.writeGeoJson(path.getPoints(), "src/main/resources/route.geojson");
    }
}