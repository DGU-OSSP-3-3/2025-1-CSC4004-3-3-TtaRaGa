package com.example.ttaraga.ttaraga.service.settingDensity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

/*
밀집도의 범위를 설정하기 위해 GeoJSON을 만드는 코드, DensityAreaInfo DB에 들어갈 예정
 */

@Component
public class GeoJsonGernerator {

    private final ObjectMapper objectMapper;

    public GeoJsonGernerator(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    /**
     * 주어진 위도/경도를 중심으로 정사각형 GeoJSON Polygon을 생성합니다.
     * 이 GeoJSON은 아무런 속성(properties)도 포함하지 않습니다.
     *
     * @param centerLat 위도 (예: 37.5135) - GeoJSON 사각형의 중심 위도
     * @param centerLon 경도 (예: 127.0590) - GeoJSON 사각형의 중심 경도
     * @return 생성된 GeoJSON Polygon을 나타내는 JSON 문자열. 생성 실패 시 null 반환.
     */
    public String generateSquareGeoJson(double centerLat, double centerLon) {
        // GeoJSON 사각형의 크기를 결정할 반경 (km)
        // 엑셀에서 이 값을 읽어오지 않으므로, 여기서는 고정된 기본값을 사용합니다.
        // 필요에 따라 이 값을 외부 설정(application.properties)으로 뺄 수 있습니다.
        double radiusKm = 0.5; // 예시: 0.5km (500m) 반경

        // 서울 지역의 대략적인 위도/경도 1도당 거리 (km)
        double latDegreePerKm = 1.0 / 111.0;
        double lonDegreePerKm = 1.0 / 88.0;

        // 반경에 해당하는 위도/경도 변화량 계산
        double deltaLat = radiusKm * latDegreePerKm;
        double deltaLon = radiusKm * lonDegreePerKm;

        // 정사각형 꼭짓점 좌표 계산 (경도, 위도 순서)
        double north = centerLat + deltaLat;
        double south = centerLat - deltaLat;
        double east = centerLon + deltaLon;
        double west = centerLon - deltaLon;

        // GeoJSON Feature 객체 생성
        ObjectNode geoJsonFeature = objectMapper.createObjectNode();
        geoJsonFeature.put("type", "Feature");

        // properties 필드를 완전히 제거하거나, 비어있는 ObjectNode를 넣을 수 있습니다.
        // 여기서는 완전히 제거하여 최소화합니다.
        // geoJsonFeature.set("properties", objectMapper.createObjectNode());

        // GeoJSON Geometry (도형) 정의 - Polygon 타입
        ObjectNode geometry = objectMapper.createObjectNode();
        geometry.put("type", "Polygon");

        // Polygon 좌표 정의: 외부 링 (시작점과 끝점은 동일해야 함)
        ArrayNode coordinates = objectMapper.createArrayNode();
        ArrayNode linearRing = objectMapper.createArrayNode();

        // GeoJSON은 [경도, 위도] 순서로 좌표를 정의합니다.
        linearRing.add(objectMapper.createArrayNode().add(west).add(south)); // 남서
        linearRing.add(objectMapper.createArrayNode().add(east).add(south)); // 남동
        linearRing.add(objectMapper.createArrayNode().add(east).add(north)); // 북동
        linearRing.add(objectMapper.createArrayNode().add(west).add(north)); // 북서
        linearRing.add(objectMapper.createArrayNode().add(west).add(south)); // 시작점으로 돌아와 링 닫기

        coordinates.add(linearRing); // 폴리곤은 여러 링을 가질 수 있으므로 (구멍 등) 배열에 담습니다.
        geometry.set("coordinates", coordinates);
        geoJsonFeature.set("geometry", geometry);

        try {
            // 생성된 GeoJSON Feature 객체를 예쁘게 포맷팅된 JSON 문자열로 반환
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(geoJsonFeature);
        } catch (Exception e) {
            System.err.println("GeoJSON 생성 중 오류 발생 (위도: " + centerLat + ", 경도: " + centerLon + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * 밀집도 레벨 문자열을 숫자로 된 우선순위(가중치)로 변환합니다.
     * 이 가중치는 GraphHopper의 커스텀 모델에서 도로에 불이익을 주거나 선호도를 높이는 데 사용될 수 있습니다.
     * (예: 숫자가 높을수록 더 큰 불이익)
     *
     * @param densityLevel 서울시 API에서 제공하는 밀집도 레벨 문자열 (예: "여유", "보통", "약간 붐빔", "붐빔", "매우 붐빔")
     * @return 해당 밀집도 레벨에 대한 정수형 우선순위. 알 수 없는 레벨일 경우 0을 반환.
     */
    public int getPriorityFromDensityLevel(String densityLevel) {
        if (densityLevel == null || densityLevel.trim().isEmpty()) {
            return 0; // null 또는 빈 문자열은 기본값 0
        }

        return switch (densityLevel) {
            case "매우 붐빔" -> 5;
            case "붐빔" -> 4;
            case "약간 붐빔" -> 3;
            case "보통" -> 2;
            case "여유" -> 1;
            default -> {
                System.err.println("경고: 알 수 없는 밀집도 레벨이 감지되었습니다: '" + densityLevel + "'");
                yield 0; // 정의되지 않은 레벨일 경우 기본값 0
            }
        };
    }





}

