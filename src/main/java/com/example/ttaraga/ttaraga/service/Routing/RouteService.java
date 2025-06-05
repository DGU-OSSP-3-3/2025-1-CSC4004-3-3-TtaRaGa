package com.example.ttaraga.ttaraga.service.Routing;
/*
핵심 총괄 서비스
사용자 요청을 처리하는 "최상위 조립" 서비스
아래의 서비스들을 조합해서 최종 결과를 반환
 */

import com.example.ttaraga.ttaraga.dto.RouteResultDtoTemp;
import com.example.ttaraga.ttaraga.exception.NoValidRouteFoundException;
import com.example.ttaraga.ttaraga.service.evaluation.CandidatePointGenerator;
import com.example.ttaraga.ttaraga.service.evaluation.CandidateRoute;
import com.example.ttaraga.ttaraga.service.evaluation.RouteEvaluationService;
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

    public RouteResultDtoTemp findBestRoute(GHPoint start, int timeLimitMinutes) {
        System.out.println("[DEBUG] 요청 받은 시작점: [" + start + "] / 시간 제한: " + timeLimitMinutes + "분");

        double speedMetersPerMinute = 250.0; // 시속 15km 기준
        double bufferRatio = 0.9;

        double oneWayTime = timeLimitMinutes / 2.0;
        double adjustedTime = oneWayTime * bufferRatio;
        double radiusMeters = speedMetersPerMinute * adjustedTime;

        System.out.println("[DEBUG] 계산된 반경: " + radiusMeters + "m");

        List<GHPoint> candidates = candidatePointGenerator.generate(start, radiusMeters);
        System.out.println("[DEBUG] 후보지 개수: " + candidates.size());

        List<CandidateRoute> results = new ArrayList<>();

        for (GHPoint dest : candidates) {
            System.out.printf("[DEBUG] ▶ 후보지 탐색 중: [%s]\n", dest);

            Optional<ResponsePath> pathOpt = graphhopperService.getPath(start, dest);

            if (pathOpt.isEmpty()) {
                System.out.println("[SKIP] 경로 생성 실패 - 해당 후보지로 가는 경로 없음");
                continue;
            }

            ResponsePath path = pathOpt.get();

            double timeMin = path.getTime() / 60000.0;
            if (timeMin > timeLimitMinutes) {
                System.out.printf("[SKIP] 경로 소요 시간 초과 (%.2f분 > %d분)\n", timeMin, timeLimitMinutes);
                continue;
            }

            double score = routeEvaluationService.evaluateRoute(path);
            String geoJson = graphhopperService.convertToGeoJson(path);

            System.out.println("[DEBUG] ✅ 유효한 후보지 추가: " + dest);
            System.out.printf("[DEBUG] └ 시간: %.2f분 | 점수: %.4f\n", timeMin, score);

            results.add(new CandidateRoute(dest, geoJson, score, path));
        }

        if (results.isEmpty()) {
            System.out.println("[ERROR] 유효한 후보 경로가 없습니다. ResponsePath 생성 실패 or 조건 불충족");
        }

        return results.stream()
                .max(Comparator.comparing(CandidateRoute::getScore))
                .map(r -> new RouteResultDtoTemp(r.getGeoJson(), List.of(start, r.getPoint()), r.getPath()))
                .orElseThrow(() -> new NoValidRouteFoundException("유효한 루트를 찾을 수 없습니다."));
    }

    public RouteResultDtoTemp findBestRouteBetween(GHPoint start, GHPoint end) {
        System.out.printf("[DEBUG] findBestRoute(start, end) 호출: 시작점 = [%s], 도착점 = [%s]\n", start, end);

        Optional<ResponsePath> pathOpt = graphhopperService.getPath(start, end);
        if (pathOpt.isEmpty()) {
            System.out.println("[ERROR] 두 지점 간 경로 생성 실패");
            return null;
        }

        ResponsePath path = pathOpt.get();
        double score = routeEvaluationService.evaluateRoute(path);
        String geoJson = graphhopperService.convertToGeoJson(path);

        System.out.printf("[DEBUG] ✅ 두 지점 경로 생성 완료 | 거리: %.2fm | 시간: %.2f분 | 점수: %.4f\n",
                path.getDistance(), path.getTime() / 60000.0, score);

        return new RouteResultDtoTemp(geoJson, List.of(start, end), path);
    }

    public List<CandidateRoute> findCandidateRoutes(GHPoint start, int timeLimitMinutes) {
        double speedMetersPerMinute = 250.0; // 시속 15km 기준
        double bufferRatio = 0.9;

        double oneWayTime = timeLimitMinutes / 2.0;
        double adjustedTime = oneWayTime * bufferRatio;
        double radiusMeters = speedMetersPerMinute * adjustedTime;

        System.out.println("[DEBUG] 계산된 반경: " + radiusMeters + "m");

        List<GHPoint> candidates = candidatePointGenerator.generate(start, radiusMeters);
        System.out.println("[DEBUG] 후보지 개수: " + candidates.size());

        List<CandidateRoute> results = new ArrayList<>();

        for (GHPoint dest : candidates) {
            System.out.printf("[DEBUG] ▶ 후보지 탐색 중: [%s]\n", dest);

            Optional<ResponsePath> pathOpt = graphhopperService.getPath(start, dest);

            if (pathOpt.isEmpty()) {
                System.out.println("[SKIP] 경로 생성 실패 - 해당 후보지로 가는 경로 없음");
                continue;
            }

            ResponsePath path = pathOpt.get();

            double timeMin = path.getTime() / 60000.0;
            if (timeMin > timeLimitMinutes) {
                System.out.printf("[SKIP] 경로 소요 시간 초과 (%.2f분 > %d분)\n", timeMin, timeLimitMinutes);
                continue;
            }

            double score = routeEvaluationService.evaluateRoute(path);
            String geoJson = graphhopperService.convertToGeoJson(path);

            System.out.println("[DEBUG] ✅ 유효한 후보지 추가: " + dest);
            System.out.printf("[DEBUG] └ 시간: %.2f분 | 점수: %.4f\n", timeMin, score);

            results.add(new CandidateRoute(dest, geoJson, score, path));
        }

        if (results.isEmpty()) {
            System.out.println("[ERROR] 유효한 후보 경로가 없습니다. ResponsePath 생성 실패 or 조건 불충족");
        }

        return results;
    }


}


