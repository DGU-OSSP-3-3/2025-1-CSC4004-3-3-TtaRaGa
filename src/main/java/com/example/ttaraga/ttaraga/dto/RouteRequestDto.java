package com.example.ttaraga.ttaraga.dto;

import com.graphhopper.util.shapes.GHPoint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RouteRequestDto {
    private double lat;
    private double lon;
    private int timeLimitMinutes;

    public GHPoint getStart() {
        return new GHPoint(lat, lon);
    }
}