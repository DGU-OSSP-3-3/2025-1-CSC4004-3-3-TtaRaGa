package com.example.ttaraga.ttaraga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Data;

//혼잡도 데이터 관리를 위한 Dto
@Data
public class DensityDto {

    @JsonProperty("AREA_CD")
    private long AREA_CD;

    @JsonProperty("AREA_NM")
    private String AREA_NM;

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("AREA_CONGEST_LVL")
    private String AREA_CONGEST_LVL;
}
