package com.example.ttaraga.ttaraga.service;
/*
사용자가 시작점(위도,경도) 와 시간을 주면
해당 코드에서 해당 시간 내에 갈 수 있는 범위 폴리곤으로 줌

1.IsochroneService -> 일단 시작점을 기준으로 원을 그리는 코드로 작성함
초안을 작성하고 나중에 더 업글하는식으로 진행할 필요있음 ->지금 진행 너무 느림
 */

import com.graphhopper.GraphHopper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;
import com.graphhopper.isochrone.algorithm.*;

@Service
public class IsochroneService {
    private final GraphHopper hopper;
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public IsochroneService(GraphHopper hopper) {
        this.hopper = hopper;
    }

    public Polygon createCircularRange(double lat, double lon, double minutes) {

        double speedKmh = 15.0; // 평균 자전거 속도 (km/h)
        double distanceKm = (speedKmh * minutes) / 60.0;

        int numPoints = 36; // 원형을 구성할 점 수 (10도)
        Coordinate[] coords = new Coordinate[numPoints+1];

        for(int i=0; i<numPoints; i++) {
            double angle = 2*Math.PI * i / numPoints;
            double dx = distanceKm * Math.cos(angle);
            double dy = distanceKm * Math.sin(angle);

            //위도 경도 계산
            double deltaLat = dy / 111.32;
            double deltaLon = dx / (111.32 * Math.cos(Math.toRadians(lat)));
            double pointLat = lat + deltaLat;
            double pointLon = lon + deltaLon;

            coords[i] = new Coordinate(pointLat, pointLon);
        }

        coords[numPoints] = coords[0]; //다각형 닫기

        LinearRing ring = geometryFactory.createLinearRing(coords);
        return geometryFactory.createPolygon(ring);
        //원형에 가까운 다각형 (36개의 꼭지점)
        //리턴값 GeoJson화 시키면 지도에 시각화 가능 -> jts2geojson라이브러리 이용
    }

}
