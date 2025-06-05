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
        InputStream input = getClass().getResourceAsStream("/bikepaths.geojson");
        if (input == null) throw new IOException("[ERROR] /bikepaths.geojson 파일을 classpath에서 찾을 수 없습니다.");

        FeatureJSON fjson = new FeatureJSON();
        SimpleFeatureCollection collection = (SimpleFeatureCollection) fjson.readFeatureCollection(input);

        GeometryFactory geometryFactory = new GeometryFactory();
        List<LineString> lineStrings = new ArrayList<>();

        try (SimpleFeatureIterator it = collection.features()) {
            while (it.hasNext()) {
                SimpleFeature feature = it.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();

                if (geometry instanceof LineString) {
                    lineStrings.add((LineString) geometry);
                } else if (geometry instanceof MultiLineString) {
                    MultiLineString mls = (MultiLineString) geometry;
                    for (int i = 0; i < mls.getNumGeometries(); i++) {
                        lineStrings.add((LineString) mls.getGeometryN(i));
                    }
                }
            }
        }

        if (lineStrings.isEmpty()) throw new IOException("[ERROR] 자전거도로 LineString을 찾을 수 없습니다.");

        GeometryCollection geometryCollection = geometryFactory.createGeometryCollection(
                lineStrings.toArray(new LineString[0])
        );
        return geometryCollection.union();  // union으로 전체 병합
    }
}