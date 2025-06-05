package com.example.ttaraga.ttaraga.service.settingCustomModel;

import com.example.ttaraga.ttaraga.entity.DensityAreaInfo;
import com.example.ttaraga.ttaraga.repository.DensityAreaInfoRepository;
// import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import lombok.RequiredArgsConstructor;
// import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
// import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.logging.Logger;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CustomModel {
    private final DensityAreaInfoRepository densityAreaInfoRepository;
    private final ObjectMapper objectMapper;

    private final WebClient webClient = WebClient.create();

    // 로깅을 위한 Logger 인스턴스 생성
    private static final Logger logger = Logger.getLogger(CustomModel.class.getName());

    // JSON 파일 저장 경로
    private static final String JSON_FILE_PATH = "src/main/resources/custom_model.json";

    // 프로그램 시작 시 자동 실행
    @PostConstruct
    public void init() {
        logger.info("프로그램 시작 시 CustomModel 초기화 시작");
        ObjectNode customModelJson = generateCustomModelJson();
        logger.info("생성된 CustomModel JSON: " + customModelJson.toString());
        // 필요에 따라 JSON을 저장하거나 추가 작업 수행 가능
    }

    // 10분마다 JSON 파일을 다시 생성 (600,000ms = 10분)
    @Scheduled(fixedRate = 600000)
    public void scheduledGenerateAndSaveCustomModelJson() {
        logger.info("스케줄링에 의해 CustomModel JSON 재생성 시작");
        generateAndSaveCustomModelJson();
    }

    // JSON 생성 및 파일 저장 로직
    private void generateAndSaveCustomModelJson() {
        try {
            ObjectNode customModelJson = generateCustomModelJson();
            // JSON을 파일로 저장
            Files.writeString(Paths.get(JSON_FILE_PATH), customModelJson.toPrettyString());
            logger.info("CustomModel JSON 파일 생성 성공: " + JSON_FILE_PATH);
        } catch (IOException e) {
            logger.severe("CustomModel JSON 파일 생성 실패: " + e.getMessage());
        }
    }

    public ObjectNode generateCustomModelJson() {
        // 혼잡 지역만 필터링
        List<DensityAreaInfo> congestedAreas = densityAreaInfoRepository.findByDensityLevelIn(List.of("혼잡", "매우혼잡"));

        ObjectNode customModel = objectMapper.createObjectNode();

        // ------------------------
        // 1. speed 설정
        // ------------------------
        ArrayNode speedRules = objectMapper.createArrayNode();

        // 밀집 구간에서는 속도를 줄이고 회피 유도
        ObjectNode speedRule = objectMapper.createObjectNode();
        speedRule.put("if", "in_congested_zones");
        speedRule.put("multiply_by", 0.5);  // 속도를 50% 줄임.
        speedRules.add(speedRule);

        // 혼잡 지역에서는 최대 속도 제한
        ObjectNode speedLimitRule = objectMapper.createObjectNode();
        speedLimitRule.put("if", "in_congested_zones");
        speedLimitRule.put("limit_to", 30);
        speedRules.add(speedLimitRule);

        customModel.set("speed", speedRules);

        // ------------------------
        // 2. priority 설정
        // ------------------------
        ArrayNode priorityRules = objectMapper.createArrayNode();

        ObjectNode priorityRule = objectMapper.createObjectNode();
        priorityRule.put("if", "in_congested_zones");
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
        for (DensityAreaInfo area : congestedAreas) {
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
            feature.set("properties", objectMapper.createObjectNode());

            // properties (비워둠)
            features.add(feature);
        }
        congestedZone.set("features", features);
        areasNode.set("congested_zones", congestedZone);
        customModel.set("areas", areasNode);

        return customModel;
    }

//    /**
//     * 출발-도착 좌표 리스트 받아 GraphHopper에 경로 요청 보내기
//     */
//    public JsonNode requestGraphHopperRoute(List<Double[]> points) throws Exception {
//        ObjectNode requestBody = objectMapper.createObjectNode();
//
//        // points 배열에 넣기
//        ArrayNode pointsArray = objectMapper.createArrayNode();
//        for (Double[] point : points) {
//            ArrayNode coord = objectMapper.createArrayNode();
//            coord.add(point[0]);  // 경도
//            coord.add(point[1]);  // 위도
//            pointsArray.add(coord);
//        }
//        requestBody.set("points", pointsArray);
//
//        // custom_model 넣기
//        ObjectNode customModelJson = generateCustomModelJson();
//        requestBody.set("custom_model", customModelJson);
//
//        // webClient로 POST 요청 보내기
//        String graphHopperUrl = "https://graphhopper.com/maps/?point=37.546849%2C126.877618&point=&point=&point=&point=&profile=bike&layer=Omniscale";
//        Mono<JsonNode> responseMono = webClient.post()
//                .uri(graphHopperUrl)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(JsonNode.class);
//
//        return responseMono.block();
//    }
}