package com.example.ttaraga.ttaraga.dto;

import com.example.ttaraga.ttaraga.entity.Density;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Densitydto {
    private long id;

    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lng")
    private double longitude;

    @JsonProperty("density")
    private double densityLevel;

    public Densitydto() {}

    public Densitydto(long id, double latitude, double longitude, double densityLevel) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.densityLevel = densityLevel;
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDensityLevel() {
        return densityLevel;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setDensityLevel(double densityLevel) {
        this.densityLevel = densityLevel;
    }

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
