package com.example.ttaraga.ttaraga.service.evaluation;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import lombok.Getter;

@Getter
public class CandidateRoute {
    private final GHPoint point;
    private final String geoJson;
    private final double score;
    private ResponsePath path;

    private PointList points; // ✅ 추가

    // 기존 생성자에 포함되지 않았다면 세터로 설정할 수 있도록
    public void setPoints(PointList points) {
        this.points = points;
    }

    public PointList getPoints() {
        return this.points;
    }

    public CandidateRoute(GHPoint point, String geoJson, double score, ResponsePath path) {
        this.point = point;
        this.geoJson = geoJson;
        this.score = score;
        this.path = path;
        this.points = (path != null) ? path.getPoints() : null;
    }
}