package com.example.ttaraga.ttaraga.config;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.dem.SRTMGL1Provider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/*
이렇게 설정 클래스를 만들어서 GraphHopper를 빈으로 등록하자.
 */

@Configuration
@EnableScheduling
public class GraphhopperConfig {

    @Bean
    public GraphHopper hopper() {
        GraphHopper hopper = new GraphHopper();

        hopper.setOSMFile("src/main/resources/seoul-non-military.osm.pbf"); // osm 파일경로 나중에 수정 ㄱㄱ
        hopper.setGraphHopperLocation("graph-cache"); // 캐시 저장 위치

        // 반드시 importOrLoad 전에 고도 설정
        hopper.setElevation(true);
        hopper.setElevationProvider(new SRTMGL1Provider("elevation_cache"));

        hopper.setProfiles(List.of(new Profile("bike").setVehicle("bike")));
        hopper.getCHPreparationHandler().setCHProfiles(List.of(new CHProfile("bike")));

        hopper.importOrLoad(); // osm 데이터를 로딩 또는 캐시 사용

        return hopper;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
