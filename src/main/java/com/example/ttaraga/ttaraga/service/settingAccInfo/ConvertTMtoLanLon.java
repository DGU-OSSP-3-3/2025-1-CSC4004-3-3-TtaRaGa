package com.example.ttaraga.ttaraga.service.settingAccInfo;

import org.osgeo.proj4j.*;
import org.springframework.stereotype.Component;

@Component
public class ConvertTMtoLanLon {

    public static double[] TMtoLanLon(double TMX, double TMY){
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem sourceCRS = crsFactory.createFromName(("EPSG:5179"));
        CoordinateReferenceSystem targetCRS = crsFactory.createFromName("EPSG:4326");
        CoordinateTransform transformer = new
                CoordinateTransformFactory().createTransform(sourceCRS, targetCRS);
        ProjCoordinate srcCoord = new ProjCoordinate(TMX, TMY);
        ProjCoordinate dstCoord = new ProjCoordinate();
        transformer.transform(srcCoord, dstCoord);
        return new double[]{dstCoord.y,dstCoord.x};  // 변환된 위도, 경도 값에 해당.
    }
}
