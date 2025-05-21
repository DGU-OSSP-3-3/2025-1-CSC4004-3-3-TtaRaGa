package com.example.ttaraga.ttaraga.dto;

import com.example.ttaraga.ttaraga.entity.Density;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Densitydto {
    private long id;

    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lng")
    private double longitude;

    @JsonProperty("density")
    private double densityLevel;


    // Entity → DTO
    public static Densitydto fromEntity(Density density) {
        return new Densitydto(
                density.getId(),
                density.getLatitude(),
                density.getLongitude(),
                density.getDensityLevel()
        );
    }

    // DTO → Entity
    public Density toEntity() {
        Density density = new Density();
        density.setId(id);
        density.setLatitude(latitude);
        density.setLongitude(longitude);
        density.setDensityLevel(densityLevel);
        return density;
    }
}
