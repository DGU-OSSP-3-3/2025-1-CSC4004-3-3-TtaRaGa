package com.example.ttaraga.ttaraga.service.settingCustomModel;

import com.example.ttaraga.ttaraga.entity.DensityAreaInfo;
import com.example.ttaraga.ttaraga.repository.DensityAreaInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomModel {
    private final DensityAreaInfoRepository densityAreaInfoRepository;
    private final ObjectMapper objectMapper;

    public ObjectNode generateCustomModelJson(){
        // 혼잡 지역만 필터링
        List<DensityAreaInfo> congestedAreas = densityAreaInfoRepository.findByDensityLevel(List.of("혼잡", "매우혼잡"));

        ObjectNode customModel = objectMapper.createObjectNode();

        // ------------------------
        // 1. speed 설정
        // ------------------------
        ArrayNode speedRules = objectMapper.createArrayNode();

        // 밀집 구간에서는 속도를 줄이고 회피 유도
        ObjectNode speedRule = objectMapper.createObjectNode();
        speedRule.put("if", "in_congested_zone");
        speedRule.put("multiply_by", 0.5);  // 속도를 50% 줄임.
        speedRules.add(speedRule);

        // 혼잡 지역에서는 최대 속도 제한
        ObjectNode speedLimitRule = objectMapper.createObjectNode();
        speedLimitRule.put("if", "in_congested_zone");
        speedLimitRule.put("limit_to", 30);
        speedRules.add(speedLimitRule);

        customModel.set("speed", speedRules);

        // ------------------------
        // 2. priority 설정
        // ------------------------
        ArrayNode priorityRules = objectMapper.createArrayNode();

        ObjectNode priorityRule = objectMapper.createObjectNode();
        priorityRule.put("if", "in_congested_zone");
        priorityRule.put("multiply_by", 0.3);  // 우선순위 낮춤.
        priorityRules.add(priorityRule);

        customModel.set("priority", priorityRules);

        // ------------------------
        // 3. distance_influence 설정
        // ------------------------
        customModel.put("distance_influence", 100);

        // ------------------------
        // 4. areas: GeoJSON FeatureCollection 형식으로 혼잡 지역 정의
        // ------------------------
        ObjectNode areasNode = objectMapper.createObjectNode();
        ObjectNode congestedZone = objectMapper.createObjectNode();
        congestedZone.put("type", "FeatureCollection");

        ArrayNode features = objectMapper.createArrayNode();
        for(DensityAreaInfo area : congestedAreas) {
            ObjectNode feature = objectMapper.createObjectNode();
            feature.put("type", "Feature");

            // geometry
            ObjectNode geometry = objectMapper.createObjectNode();
            geometry.put("type", "Point");

            ArrayNode coordinates = objectMapper.createArrayNode();
            coordinates.add(area.getLongitude()); // 경도
            coordinates.add(area.getLatitude());  // 위도
            geometry.set("coordinates", coordinates);

            feature.set("geometry", geometry);

            // properties (비워둠)
            features.add(features);
        }
        congestedZone.set("features", features);
        areasNode.set("congested_zones", congestedZone);
        customModel.set("areas", areasNode);

        return customModel;
    }
}
