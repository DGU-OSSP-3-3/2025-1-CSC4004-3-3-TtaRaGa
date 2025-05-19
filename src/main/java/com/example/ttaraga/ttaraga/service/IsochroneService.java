package com.example.ttaraga.ttaraga.service;
/*
사용자가 시작점(위도,경도) 와 시간을 주면
해당 코드에서 해당 시간 내에 갈 수 있는 범위 폴리곤으로 줌

1.IsochroneService -> 일단 시작점을 기준으로 원을 그리는 코드로 작성함
초안을 작성하고 나중에 더 업글하는식으로 진행할 필요있음 ->지금 진행 너무 느림
 */

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;
import com.graphhopper.isochrone.algorithm.*;

@Service
public class IsochroneService {
    //private final GraphHopper hopper;

    private static final GeometryFactory geometryFactory = new GeometryFactory();


    public Polygon createCircularRange(double Lat, double Lon, double minutes) {

        // 자전거 평균 속도 15km/h → 0.25km/min
        double radiusKm = minutes * 0.25;

        // 위도/경도 기준으로 환산 (1도 ≈ 111km)
        double radiusDeg = radiusKm / 111.0;

        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();

        shapeFactory.setNumPoints(50); //원을 구성할 점 개수
        shapeFactory.setCentre(new Coordinate(Lon,Lat));
        shapeFactory.setSize(radiusDeg*2); //지름 기준

        return shapeFactory.createCircle(); //Polygon 객체로 리턴
    }




}
