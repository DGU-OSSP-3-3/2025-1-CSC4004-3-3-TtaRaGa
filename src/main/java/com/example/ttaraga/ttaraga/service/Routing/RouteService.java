package com.example.ttaraga.ttaraga.service.Routing;
/*
핵심 총괄 서비스
사용자 요청을 처리하는 "최상위 조립" 서비스
아래의 서비스들을 조합해서 최종 결과를 반환
 */

import com.example.ttaraga.ttaraga.dto.RouteResultDto;
import com.example.ttaraga.ttaraga.exception.NoValidRouteFoundException;
import com.example.ttaraga.ttaraga.service.CandidatePointGenerator;
import com.example.ttaraga.ttaraga.service.CandidateRoute;
import com.example.ttaraga.ttaraga.service.RouteEvaluationService;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RouteService {
    private final CandidatePointGenerator candidatePointGenerator;
    private final GraphhopperService graphhopperService;
    private final RouteEvaluationService routeEvaluationService;

    public RouteService(
            CandidatePointGenerator candidatePointGenerator,
            GraphhopperService graphhopperService,
            RouteEvaluationService routeEvaluationService
    ) {
        this.candidatePointGenerator = candidatePointGenerator;
        this.graphhopperService = graphhopperService;
        this.routeEvaluationService = routeEvaluationService;
    }

//    public RouteResultDto findBestRoute(GHPoint start, int timeLimitMinutes) {
//        List<GHPoint> candidates = candidatePointGenerator.generate(start, 2000); // 2km 기준
//        List<CandidateRoute> results = new ArrayList<>();
//
//        for (GHPoint dest : candidates) {
//            Optional<ResponsePath> pathOpt = graphhopperService.getPath(start, dest);
//            if (pathOpt.isEmpty()) continue;
//
//            ResponsePath path = pathOpt.get();
//            if (path.getTime() > timeLimitMinutes * 60 * 1000) continue;
//
//            String geoJson = graphhopperService.convertToGeoJson(path);
//            System.out.println(" [DEBUG] 생성된 geoJson: " + geoJson);
//            double score = routeEvaluationService.evaluateRoute(geoJson);
//            results.add(new CandidateRoute(dest, geoJson, score));
//        }
//
//        return results.stream()
//                .max(Comparator.comparing(CandidateRoute::getScore))
//                .map(r -> new RouteResultDto(r.getGeoJson(), List.of(start, r.getPoint())))
//                .orElseThrow(() -> new NoValidRouteFoundException("조건을 만족하는 루트를 찾을 수 없습니다."));
//    }

    public RouteResultDto findBestRoute(GHPoint start, int timeLimitMinutes) {
        System.out.println("[DEBUG] 요청 받은 시작점: [" + start + "] / 시간 제한: " + timeLimitMinutes + "분");

        List<GHPoint> candidates = candidatePointGenerator.generate(start, 2000); // 2km 기준
        List<CandidateRoute> results = new ArrayList<>();

        for (GHPoint dest : candidates) {
            Optional<ResponsePath> pathOpt = graphhopperService.getPath(start, dest);
            if (pathOpt.isEmpty()) continue;
            ResponsePath path = pathOpt.get();
            if (path.getTime() > timeLimitMinutes * 60 * 1000) continue;

            String geoJson = graphhopperService.convertToGeoJson(path);

            System.out.println("[DEBUG] 경로 후보: 시작점 [" + start + "] → 도착점 [" + dest + "]");
            System.out.println("[DEBUG] 생성된 geoJson: " + geoJson);

            double score = routeEvaluationService.evaluateRoute(geoJson);
            results.add(new CandidateRoute(dest, geoJson, score));
        }

        System.out.println("[DEBUG] 루트 보내기 " + results.stream().max(Comparator.comparing(CandidateRoute::getScore)).map(r -> new RouteResultDto(r.getGeoJson(), List.of(start, r.getPoint()))));

        return results.stream()
                .max(Comparator.comparing(CandidateRoute::getScore))
                .map(r -> new RouteResultDto(r.getGeoJson(), List.of(start, r.getPoint())))
                .orElseThrow(() -> new NoValidRouteFoundException("유효한 루트를 찾을 수 없습니다."));
    }


}
