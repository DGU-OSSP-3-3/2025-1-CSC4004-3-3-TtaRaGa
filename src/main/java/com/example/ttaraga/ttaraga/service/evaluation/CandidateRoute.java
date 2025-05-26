package com.example.ttaraga.ttaraga.service.evaluation;

import com.graphhopper.util.shapes.GHPoint;
import lombok.Getter;

@Getter
public class CandidateRoute {
    private final GHPoint point;
    private final String geoJson;
    private final double score;

    public CandidateRoute(GHPoint point, String geoJson, double score) {
        this.point = point;
        this.geoJson = geoJson;
        this.score = score;
    }

}