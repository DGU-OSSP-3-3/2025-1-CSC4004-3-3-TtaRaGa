package com.example.ttaraga.ttaraga.dto;

import com.graphhopper.util.shapes.GHPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    private GHPoint start; //출발지
    private GHPoint end; //도착지
    private List<GHPoint> waypoints; //경유지
}
