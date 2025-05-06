package com.example.ttaraga.ttaraga;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.CustomModel;

public class GraphBuilder {
    public static void main(String[] args) {
        GraphHopper hopper = new GraphHopper();

        hopper.setOSMFile("src/main/resources/seoul-non-military.osm.pbf"); // OSM PBF 경로
        hopper.setGraphHopperLocation("graph-cache"); // 그래프 데이터 저장 위치

        hopper.setProfiles(
                new Profile("bike")
                        .setVehicle("bike")
                        .setWeighting("custom") // 최신 버전에서는 fastest 대신 custom
                        .setCustomModel(new CustomModel())   // 기본 모델 사용
                        .setTurnCosts(false)
        );

        hopper.importOrLoad(); // 그래프 생성
        System.out.println("그래프 생성 완료!");
    }
}