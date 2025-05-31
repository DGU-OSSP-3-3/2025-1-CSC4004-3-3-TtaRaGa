package com.example.ttaraga.ttaraga.service.evaluation;

import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.util.List;

@Component
public class BikePathEvaluator {
    private final STRtree index;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private boolean debugSaved = false;

    public BikePathEvaluator(Geometry bikePathLayer) {
        index = new STRtree();
        for (int i = 0; i < bikePathLayer.getNumGeometries(); i++) {
            Geometry geom = bikePathLayer.getGeometryN(i);
            if (geom != null && !geom.isEmpty()) {
                index.insert(geom.getEnvelopeInternal(), geom);
            }
        }
        index.build();
        System.out.println("[DEBUG] !! 인덱스에 삽입된 geometry 수: " + index.size());
    }

    public double evaluate(LineString segment) {
        Envelope envelope = segment.getEnvelopeInternal();
        envelope.expandBy(0.0001); // 약 10~11m 확장
        List<Geometry> nearby = index.query(envelope);

        if (segment.getLength() == 0) return 0.0;

        double matched = 0.0;

        for (Geometry path : nearby) {
            Geometry buffered = segment.buffer(0.001); // 약 100m 넓이 (테스트 조정 가능)
            Geometry intersection = buffered.intersection(path);
            matched += intersection.getLength();

            // 디버깅용 GeoJSON은 처음 매칭된 것만 저장
            if (!debugSaved && intersection.getLength() > 0) {
                saveDebugGeoJSON(segment, path, buffered);
                debugSaved = true;
            }
        }

        double ratio = matched / segment.getLength();
        return ratio * 10.0; // 가중치 부여된 점수 반환
    }

    private void saveDebugGeoJSON(Geometry segment, Geometry path, Geometry buffered) {
        try {
            GeometryJSON gjson = new GeometryJSON();
            gjson.write(segment, new FileWriter("debug/debug_segment.geojson"));
            gjson.write(path, new FileWriter("debug/debug_path.geojson"));
            gjson.write(buffered, new FileWriter("debug/debug_buffer.geojson"));
            System.out.println("[DEBUG] 디버깅 GeoJSON 저장 완료");
        } catch (Exception e) {
            System.err.println("[ERROR] GeoJSON 저장 실패: " + e.getMessage());
        }
    }
}