package com.example.ttaraga.ttaraga.trashCode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/*
api에서 값을 가져올때 사용하는 dto

json 으로 가져올때 어떻게 오는가?
Response code: 200
{"rentBikeStatus":
    {"list_total_count":5,
        "RESULT":
        {"CODE":"INFO-000","MESSAGE":"정상 처리되었습니다."},
        "row":[
                {
                "rackTotCnt":"15",
                "stationName":"102. 망원역 1번출구 앞",
                "parkingBikeTotCnt":"16","shared":"107",
                "stationLatitude":"37.55564880",
                "stationLongitude":"126.91062927",
                "stationId":"ST-4"
                }
              ,{"rackTotCnt":"14","stationName":"103. 망원역 2번출구 앞","parkingBikeTotCnt":"19","shared":"136","stationLatitude":"37.55495071","stationLongitude":"126.91083527","stationId":"ST-5"}
              ,{"rackTotCnt":"13","stationName":"104. 합정역 1번출구 앞","parkingBikeTotCnt":"1","shared":"8","stationLatitude":"37.55073929","stationLongitude":"126.91508484","stationId":"ST-6"}
              ,{"rackTotCnt":"5","stationName":"105. 합정역 5번출구 앞","parkingBikeTotCnt":"1","shared":"20","stationLatitude":"37.55000687","stationLongitude":"126.91482544","stationId":"ST-7"}
              ,{"rackTotCnt":"12","stationName":"106. 합정역 7번출구 앞","parkingBikeTotCnt":"3","shared":"25","stationLatitude":"37.54864502","stationLongitude":"126.91282654","stationId":"ST-8"}
              ]
    }
}
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BikeAPIResponse {
    @JsonProperty("rentBikeStatus")
    private RentBikeStatus bikeStatus;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RentBikeStatus{
        @JsonProperty("list_total_count")
        private int listTotalCount;
        @JsonProperty("RESULT")
        private ApiResult result;
        @JsonProperty("row")
        List<StationRow> row;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResult {
        @JsonProperty("CODE")
        private String code;
        @JsonProperty("MESSAGE")
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StationRow {
        @JsonProperty("rackTotCnt")
        private String rackTotCnt;
        @JsonProperty("stationName")
        private String stationName;
        @JsonProperty("parkingBikeTotCnt")
        private String parkingBikeTotCnt;
        @JsonProperty("shared")
        private String shared;
        @JsonProperty("stationLatitude")
        private String stationLatitude;
        @JsonProperty("stationLongitude")
        private String stationLongitude;
        @JsonProperty("stationId")
        private String stationId;
    }
}
