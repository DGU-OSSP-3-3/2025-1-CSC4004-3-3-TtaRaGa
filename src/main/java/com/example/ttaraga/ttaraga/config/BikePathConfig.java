package com.example.ttaraga.ttaraga.config;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class BikePathConfig {

    @Bean
    public Geometry bikePathLayer() throws IOException {
        // 1. classpath에서 simplified geojson 파일 로딩
        InputStream input = getClass().getResourceAsStream("/bikepaths.geojson");
        if (input == null) {
            throw new IOException("[ERROR] /bikepaths.geojson 파일을 classpath에서 찾을 수 없습니다.");
        }

        // 2. GeoJSON 파싱
        FeatureJSON fjson = new FeatureJSON();
        SimpleFeatureCollection collection = (SimpleFeatureCollection) fjson.readFeatureCollection(input);

        // 3. GeometryFactory 및 결과 리스트
        GeometryFactory geometryFactory = new GeometryFactory();
        List<LineString> lineStrings = new ArrayList<>();

        int featureCount = 0;
        int lineCount = 0;

        // 4. 각 Feature에서 LineString 추출
        try (SimpleFeatureIterator it = collection.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();

                if (geometry instanceof LineString) {
                    lineStrings.add((LineString) geometry);
                    featureCount++;
                    lineCount++;
                }
            }
        }

        System.out.println("[DEBUG] 자전거 도로 Feature 수: " + featureCount + "개");
        System.out.println("[DEBUG] 추출된 LineString 수: " + lineCount + "개");

        // 5. 비어있을 경우 오류
        if (lineStrings.isEmpty()) {
            throw new IOException("[ERROR] 유효한 자전거도로 LineString을 하나도 찾지 못했습니다.");
        }

        // 6. GeometryCollection → Union
        GeometryCollection geometryCollection = geometryFactory.createGeometryCollection(
                lineStrings.toArray(new LineString[0])
        );
        Geometry union = geometryCollection.union();

        System.out.println("[DEBUG] GeometryCollection union 생성 완료, 총 길이: " + union.getLength());
        return union;
    }
}