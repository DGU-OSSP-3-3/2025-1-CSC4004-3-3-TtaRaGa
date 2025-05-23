package com.example.ttaraga.ttaraga.dto;

import com.graphhopper.util.shapes.GHPoint;

public class RouteRequestDto {
    private GHPoint start;
    private int timeLimitMinutes;

    public GHPoint getStart() {
        return start;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }
}