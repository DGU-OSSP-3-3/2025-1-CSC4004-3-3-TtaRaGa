package com.example.ttaraga.ttaraga.dto;

import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.dto.DensityDto;
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

    public List<DensityDto> getDensityData() {
        return densityData.getDensityRow();
    }

    public List<BikeDto> getBikeData() {
        return bikeData.getBikeRow();
    }
}

@Getter
@Setter
class DensityData {
    @JsonProperty("row")
    private List<DensityDto> Density_row;
    public List<DensityDto> getDensityRow() {
        return Density_row;
    }
}

@Getter
@Setter
class BikeData {
    @JsonProperty("row")
    private List<BikeDto> Bike_row;
    public List<BikeDto> getBikeRow() {
        return Bike_row;
    }
}