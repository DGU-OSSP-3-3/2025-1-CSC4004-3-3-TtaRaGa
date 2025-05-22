package com.example.ttaraga.ttaraga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.transform.Result;
import java.util.List;

/**
 * 서울시 실시간 도시데이터(인구 밀집도) API 응답의 최상위 DTO.
 * 업데이트된 JSON 응답 구조에 맞춰 필드를 매핑합니다.
 *
 * 현재 JSON 응답 예시:
 * {
 * "list_total_count": 1,
 * "RESULT": {
 * "RESULT.CODE": "INFO-000",
 * "RESULT.MESSAGE": "정상 처리되었습니다."
 * },
 * "CITYDATA": { // <-- 최상위 키가 "CITYDATA"로 변경됨
 * "AREA_NM": "광화문·덕수궁",
 * "AREA_CD": "POI009",
 * "LIVE_PPLTN_STTS": [ // <-- 인구 밀집도 정보가 이 배열 안에 있음
 * {
 * "AREA_NM": "광화문·덕수궁",
 * "AREA_CONGEST_LVL": "붐빔",
 * "AREA_CONGEST_MSG": "사람들이 몰려있을 가능성이 매우 크고...",
 * // ... 기타 인구 관련 필드
 * }
 * ],
 * "ROAD_TRAFFIC_STTS": { ... } // 도로 교통 정보
 * }
 * }
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CityDataResponse_NEW {

    @JsonProperty("list_total_count")
    private int listTotalCount;

    @JsonProperty("RESULT")
    private Result result;

    // ✅ 최상위 키가 "CITYDATA"로 변경되었습니다.
    @JsonProperty("CITYDATA")
    private CityData cityData; // 내부 클래스 CityData 타입

    // --- 내부 클래스 정의 ---

    /**
     * "RESULT" 키에 해당하는 JSON 객체를 매핑하는 내부 클래스.
     * 필드명에 점(.)이 포함되어 있어 @JsonProperty 사용
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        @JsonProperty("RESULT.CODE")
        private String code;

        @JsonProperty("RESULT.MESSAGE")
        private String message;
    }

    /**
     * "CITYDATA" 키에 해당하는 JSON 객체를 매핑하는 내부 클래스.
     * 이 클래스 안에 지역명, 인구 밀집도 현황, 도로 교통 현황 등의 정보가 있습니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityData {
        @JsonProperty("AREA_NM")
        private String areaNm; // 도시 이름

        @JsonProperty("AREA_CD")
        private String areaCd; // 도시 코드

        // ✅ "LIVE_PPLTN_STTS"는 배열 형태이며, 실제 밀집도 정보가 들어있습니다.
        @JsonProperty("LIVE_PPLTN_STTS")
        private List<LivePopulationStatus> livePopulationStatus;

        // "ROAD_TRAFFIC_STTS"도 별도의 DTO로 매핑할 수 있지만, 여기서는 간략하게 생략합니다.
        // 만약 도로 교통 정보도 필요하다면 아래와 같이 추가 DTO를 정의해야 합니다.
        @JsonProperty("ROAD_TRAFFIC_STTS")
        private RoadTrafficStatus roadTrafficStatus;
    }

    /**
     * "LIVE_PPLTN_STTS" 배열 안에 있는 각 인구 현황 항목을 매핑하는 내부 클래스.
     * 여기에 AREA_CONGEST_LVL이 있습니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LivePopulationStatus {
        @JsonProperty("AREA_NM")
        private String areaNm;

        @JsonProperty("AREA_CD")
        private String areaCd;

        @JsonProperty("AREA_CONGEST_LVL")
        private String areaCongestLvl; // 인구 밀집도 레벨 (예: "여유", "보통", "붐빔", "매우 붐빔")

        @JsonProperty("AREA_CONGEST_MSG")
        private String areaCongestMsg; // 인구 밀집도 메시지

        @JsonProperty("AREA_PPLTN_MIN")
        private String areaPpltnMin; // 최소 인구 (String으로 오는 경우)

        @JsonProperty("AREA_PPLTN_MAX")
        private String areaPpltnMax; // 최대 인구 (String으로 오는 경우)

        @JsonProperty("MALE_PPLTN_RATE")
        private String malePpltnRate; // 남성 인구 비율

        @JsonProperty("FEMALE_PPLTN_RATE")
        private String femalePpltnRate; // 여성 인구 비율

        // 연령대별 인구 비율도 필요하면 추가합니다.
        @JsonProperty("PPLTN_RATE_0") private String ppltnRate0;
        @JsonProperty("PPLTN_RATE_10") private String ppltnRate10;
        @JsonProperty("PPLTN_RATE_20") private String ppltnRate20;
        @JsonProperty("PPLTN_RATE_30") private String ppltnRate30;
        @JsonProperty("PPLTN_RATE_40") private String ppltnRate40;
        @JsonProperty("PPLTN_RATE_50") private String ppltnRate50;
        @JsonProperty("PPLTN_RATE_60") private String ppltnRate60;
//        @JsonProperty("PPLTN_RATE_70") private String ppltnRate70;

        @JsonProperty("RESNT_PPLTN_RATE")
        private String resntPpltnRate; // 상주 인구 비율

        @JsonProperty("NON_RESNT_PPLTN_RATE")
        private String nonResntPpltnRate; // 비상주 인구 비율

        @JsonProperty("REPLACE_YN")
        private String replaceYn; // 대체 여부

        @JsonProperty("PPLTN_TIME")
        private String ppltnTime; // 인구 데이터 기준 시간

        @JsonProperty("FCST_YN")
        private String fcstYn; // 예측 데이터 존재 여부

        // ✅ "FCST_PPLTN"은 시간별 예측 인구 데이터 배열입니다.
        @JsonProperty("FCST_PPLTN")
        private List<ForecastPopulation> fcstPopulation;
    }

    /**
     * "FCST_PPLTN" 배열 안에 있는 각 예측 인구 항목을 매핑하는 내부 클래스.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastPopulation {
        @JsonProperty("FCST_TIME")
        private String fcstTime;

        @JsonProperty("FCST_CONGEST_LVL")
        private String fcstCongestLvl;

        @JsonProperty("FCST_PPLTN_MIN")
        private String fcstPpltnMin;

        @JsonProperty("FCST_PPLTN_MAX")
        private String fcstPpltnMax;
    }

    /**
     * "ROAD_TRAFFIC_STTS" 키에 해당하는 도로 교통 현황을 매핑하는 내부 클래스.
     * 여기에는 평균 도로 데이터와 각 도로 링크별 교통 현황이 있습니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoadTrafficStatus {
        @JsonProperty("AVG_ROAD_DATA")
        private AvgRoadData avgRoadData;

        @JsonProperty("ROAD_TRAFFIC_STTS")
        private List<RoadTrafficItem> roadTrafficItems;
    }

    /**
     * "AVG_ROAD_DATA" 키에 해당하는 평균 도로 교통 데이터를 매핑하는 내부 클래스.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvgRoadData {
        @JsonProperty("ROAD_MSG")
        private String roadMsg;

        @JsonProperty("ROAD_TRAFFIC_IDX")
        private String roadTrafficIdx;

        @JsonProperty("ROAD_TRAFFIC_SPD")
        private int roadTrafficSpd; // 속도 (정수)

        @JsonProperty("ROAD_TRAFFIC_TIME")
        private String roadTrafficTime;
    }

    /**
     * "ROAD_TRAFFIC_STTS" 배열 안에 있는 각 도로 링크별 교통 현황을 매핑하는 내부 클래스.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoadTrafficItem {
        @JsonProperty("LINK_ID") private String linkId;
        @JsonProperty("ROAD_NM") private String roadNm;
        @JsonProperty("START_ND_CD") private String startNdCd;
        @JsonProperty("START_ND_NM") private String startNdNm;
        @JsonProperty("START_ND_XY") private String startNdXy;
        @JsonProperty("END_ND_CD") private String endNdCd;
        @JsonProperty("END_ND_NM") private String endNdNm;
        @JsonProperty("END_ND_XY") private String endNdXy;
        @JsonProperty("DIST") private String dist; // 거리 (String으로 오는 경우)
        @JsonProperty("SPD") private String spd; // 속도 (String으로 오는 경우)
        @JsonProperty("IDX") private String idx; // 지수
        @JsonProperty("XYLIST") private String xyList; // 좌표 리스트
    }


}
