package com.example.ttaraga.ttaraga.dto;

import com.example.ttaraga.ttaraga.dto.Bikedto;
import com.example.ttaraga.ttaraga.dto.Densitydto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CityDataResponse {
    @JsonProperty("CITYDATA")
    private CityData cityData;
}

@Getter
@Setter
class CityData {
    @JsonProperty("densityData")
    private DensityData densityData;

    @JsonProperty("bikeData")
    private BikeData bikeData;
}

@Getter
@Setter
class DensityData {
    @JsonProperty("row")
    private List<Densitydto> row;
}

@Getter
@Setter
class BikeData {
    @JsonProperty("row")
    private List<Bikedto> row;
}