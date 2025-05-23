package com.example.ttaraga.ttaraga.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
//ㅓㅓㅓㅓ
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BikeDto {
    @JsonProperty("stationId")
    private String stationId;

    @JsonProperty("parkingBikeTotCnt")
    @JsonDeserialize(using = StringToLongDeserializer.class) // 여기에 커스텀 Deserializer 적용
    private long parkingBikeTotCnt;

    @JsonProperty("stationLatitude")
    private double stationLatitude;

    @JsonProperty("stationLongitude")
    private double stationLongitude;

    @JsonProperty("stationName")
    private String stationName;
}