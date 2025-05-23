package com.example.ttaraga.ttaraga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CityData {
    @JsonProperty("densityData")
    private DensityData densityData;

    @JsonProperty("bikeData")
    private BikeData bikeData;

    // 오류 해결 용 임시
    public List<Densitydto> getRow() {
        // TODO Auto-generated method stub
        return densityData.getRow();
    }
}
