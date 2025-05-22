package com.example.ttaraga.ttaraga.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttractingPlaceDto {
    private String placeId;

    private String placeName;

    private double placeLatitude;

    private double placeLongitude;

    private String detail;
}