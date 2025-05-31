package com.example.ttaraga.ttaraga.service.evaluation;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import lombok.Getter;

@Getter
public class CandidateRoute {
    private final GHPoint point;
    private final String geoJson;
    private final double score;
    private ResponsePath path;


    public CandidateRoute(GHPoint point, String geoJson, double score, ResponsePath path) {
        this.point = point;
        this.geoJson = geoJson;
        this.score = score;
        this.path = path;
    }


}