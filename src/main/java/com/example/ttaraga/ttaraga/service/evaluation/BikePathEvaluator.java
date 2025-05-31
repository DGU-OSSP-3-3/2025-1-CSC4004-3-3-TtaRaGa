package com.example.ttaraga.ttaraga.service.evaluation;

import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class BikePathEvaluator {

    private final STRtree index;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public BikePathEvaluator(@Qualifier("bikePathLayer") Geometry bikePathLayer) {
        this.index = new STRtree();
        insertIntoIndex(bikePathLayer);
        index.build();
        System.out.println("[DEBUG] 인덱스 빌드 완료: 총 " + index.size() + "개");
    }

    // 🔁 모든 geometry 타입을 재귀적으로 인덱스에 삽입
    private void insertIntoIndex(Geometry geom) {
        if (geom instanceof LineString) {
            index.insert(geom.getEnvelopeInternal(), geom);
        } else if (geom instanceof GeometryCollection) {
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                insertIntoIndex(geom.getGeometryN(i));
            }
        } else {
            System.out.println("[WARN] 인식하지 못한 geometry 타입: " + geom.getGeometryType());
        }
    }

    public double evaluate(LineString segment) {
        double segmentLength = segment.getLength();
        if (segmentLength == 0) return 0.0;

        Envelope envelope = segment.getEnvelopeInternal();
        envelope.expandBy(0.0003); // 약 30m
        List<Geometry> nearby = index.query(envelope);

        double matched = 0.0;
        boolean debugSaved = false;

        for (Geometry path : nearby) {
            Geometry intersection = segment.intersection(path);
            double len = intersection.getLength();

            if (len == 0 && segment.distance(path) < 0.0003) {
                matched += segmentLength;
            } else {
                matched += len;
            }

            if (!debugSaved) {
                try {
                    GeometryJSON gjson = new GeometryJSON();
                    new File("debug").mkdirs();
                    gjson.write(segment, new FileWriter("debug/debug_segment.geojson"));
                    gjson.write(path, new FileWriter("debug/debug_path.geojson"));
                    gjson.write(segment.buffer(0.0003), new FileWriter("debug/debug_buffer.geojson"));
                    debugSaved = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return matched / segmentLength;
    }
}