package com.example.ttaraga.ttaraga;

import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.PointList;

import java.io.FileWriter;
import java.io.IOException;

public class GeoJsonExporter {

    public static void writeGeoJson(PointList pointList, String filePath) {
        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{\n");
        geoJson.append("\"type\": \"Feature\",\n");
        geoJson.append("\"geometry\": {\n");
        geoJson.append("\"type\": \"LineString\",\n");
        geoJson.append("\"coordinates\": [\n");

        for (int i = 0; i < pointList.size(); i++) {
            GHPoint p = pointList.get(i);
            geoJson.append("[").append(p.lon).append(", ").append(p.lat).append("]");
            if (i < pointList.size() - 1) geoJson.append(",");
            geoJson.append("\n");
        }

        geoJson.append("]\n");
        geoJson.append("},\n");
        geoJson.append("\"properties\": {}\n");
        geoJson.append("}");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(geoJson.toString());
            System.out.println("GeoJSON 파일 저장 완료: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}