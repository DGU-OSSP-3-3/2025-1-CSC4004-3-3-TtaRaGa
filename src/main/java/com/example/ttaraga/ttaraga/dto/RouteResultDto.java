package com.example.ttaraga.ttaraga.dto;

import com.graphhopper.util.shapes.GHPoint;
import java.util.List;

public class RouteResultDto {
    private String geoJson;
    private List<GHPoint> points;

    public RouteResultDto(String geoJson, List<GHPoint> points) {
        this.geoJson = geoJson;
        this.points = points;
    }

    public String getGeoJson() { return geoJson; }
    public List<GHPoint> getPoints() { return points; }
}