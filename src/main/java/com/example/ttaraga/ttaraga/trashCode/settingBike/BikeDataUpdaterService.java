package com.example.ttaraga.ttaraga.trashCode.settingBike;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BikeDataUpdaterService {

    /*
    private final BikeRepository bikeRepository;
    private final BikeNewAPIClient bikeNewAPIClient; // BikeNewAPIClient 주입

    private static final int MAX_PER_PAGE = 1000; // 서울시 따릉이 API는 한 번에 최대 1000개 데이터 제공



    @Transactional
    public void updateAllBikeStations() {
        System.out.println("--- 따릉이 스테이션 데이터 업데이트 시작: " + System.currentTimeMillis() + " ---");
        long startTime = System.currentTimeMillis();

        try {
            // 1. 따릉이 API로부터 전체 스테이션 개수 조회
            int totalCount = bikeNewAPIClient.getTotalStationCount();
            if (totalCount == 0) {
                System.out.println("따릉이 스테이션 총 개수를 가져올 수 없습니다. API 응답을 확인하세요.");
                return; // 총 개수를 가져오지 못하면 업데이트 중단
            }
            System.out.println("총 따릉이 스테이션 개수: " + totalCount);

            // 2. 전체 데이터를 페이지네이션하여 가져오기
            int totalPages = (int) Math.ceil((double) totalCount / MAX_PER_PAGE);
            List<Bike> allBikes = new ArrayList<>(); // 모든 페이지의 Bike 엔티티를 담을 리스트

            for (int i = 0; i < totalPages; i++) {
                int startIdx = (i * MAX_PER_PAGE) + 1;
                // 마지막 페이지의 endIdx는 totalCount를 초과하지 않도록 보정
                int endIdx = Math.min((i + 1) * MAX_PER_PAGE, totalCount);

                System.out.println(String.format("API 호출 중: 인덱스 %d 부터 %d 까지", startIdx, endIdx));
                Optional<BikeAPIResponse> responseOpt = bikeNewAPIClient.fetchBikeStations(startIdx, endIdx);

                // 3. API 응답 확인 및 데이터 변환
                if (responseOpt.isPresent() &&
                        responseOpt.get().getBikeStatus() != null && // DTO에서 rentBikeStatus 필드 접근
                        responseOpt.get().getBikeStatus().getRow() != null) { // row 리스트 접근

                    List<BikeAPIResponse.StationRow> stationRows = responseOpt.get().getBikeStatus().getRow();
                    List<Bike> pageBikes = convertStationRowsToBikes(s);
                    allBikes.addAll(pageBikes);
                    System.out.println(String.format("페이지 %d (%d-%d) 데이터 %d개 수집 완료.", i + 1, startIdx, endIdx, stationRows.size()));
                } else {
                    System.err.println(String.format("따릉이 API 페이지 데이터 가져오기 실패 또는 데이터 없음 (인덱스: %d-%d).", startIdx, endIdx));
                }
            }

            // 4. 수집된 모든 데이터를 DB에 저장하거나 업데이트 (Upsert)
            saveOrUpdateBikes(allBikes);
            long endTime = System.currentTimeMillis();
            System.out.println(String.format("--- 따릉이 스테이션 데이터 업데이트 완료. 총 %d개 저장/업데이트됨. 소요 시간: %dms ---", allBikes.size(), (endTime - startTime)));

        } catch (Exception e) {
            System.err.println("따릉이 스테이션 데이터 업데이트 중 심각한 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 예외 스택 트레이스 출력
        }
    }


    private List<Bike> convertStationRowsToBikes(List<StationRow> rows) {
        List<Bike> bikes = new ArrayList<>();
        for (StationRow row : rows) {
            try {
                Bike bike = new Bike();
                bike.setApiStationId(row.getStationId());
                bike.setStationName(row.getStationName());
                bike.setParkingBikeTotCnt(parseLong(row.getParkingBikeTotCnt()));
                bike.setStationLatitude(parseDouble(row.getStationLatitude()));
                bike.setStationLongitude(parseDouble(row.getStationLongitude()));
                // 필요하다면 다른 필드도 추가:
                // bike.setRackTotCnt(parseLong(row.getRackTotCnt()));
                // bike.setShared(row.getShared());
                bikes.add(bike);
            } catch (NumberFormatException e) {
                System.err.println("따릉이 스테이션 데이터 파싱 오류 (숫자 변환 실패): " + row.getStaion() + ", " + e.getMessage());
            } catch (Exception e) {
                System.err.println("따릉이 스테이션 데이터 변환 중 알 수 없는 오류: " + row.getStationId() + ", " + e.getMessage());
            }
        }
        return bikes;
    }


    private void saveOrUpdateBikes(List<Bike> newBikes) {
        int updatedCount = 0;
        int insertedCount = 0;

        for (Bike newBike : newBikes) {
            // apiStationId를 기준으로 기존 Bike 엔티티를 찾습니다.
            Optional<Bike> existingBikeOpt = bikeRepository.findByApiStationId(newBike.getApiStationId());

            if (existingBikeOpt.isPresent()) {
                // 기존 데이터가 있으면 업데이트 (dirty checking 활용)
                Bike existingBike = existingBikeOpt.get();
                // 변경 사항이 있을 때만 업데이트하여 불필요한 DB 트랜잭션 줄이기
                if (!existingBike.getStationName().equals(newBike.getStationName()) ||
                        !existingBike.getParkingBikeTotCnt().equals(newBike.getParkingBikeTotCnt()) ||
                        !existingBike.getStationLatitude().equals(newBike.getStationLatitude()) ||
                        !existingBike.getStationLongitude().equals(newBike.getStationLongitude()))
                {
                    existingBike.setStationName(newBike.getStationName());
                    existingBike.setParkingBikeTotCnt(newBike.getParkingBikeTotCnt());
                    existingBike.setStationLatitude(newBike.getStationLatitude());
                    existingBike.setStationLongitude(newBike.getStationLongitude());
                    // 다른 필드도 필요하다면 업데이트
                    bikeRepository.save(existingBike); // save 메서드가 upsert 역할을 수행
                    updatedCount++;
                }
            } else {
                // 새 데이터면 삽입
                bikeRepository.save(newBike);
                insertedCount++;
            }
        }
        System.out.println(String.format("DB 업데이트 결과: 삽입 %d개, 업데이트 %d개", insertedCount, updatedCount));
    }

    private Long parseLong(String value) {
        try {
            return value != null && !value.isEmpty() ? Long.parseLong(value) : 0L; // null 대신 기본값 0L 반환 고려
        } catch (NumberFormatException e) {
            System.err.println("parseLong 오류: '" + value + "'는 유효한 Long 숫자가 아닙니다.");
            return 0L; // 파싱 실패 시 0L 반환
        }
    }


    private Double parseDouble(String value) {
        try {
            return value != null && !value.isEmpty() ? Double.parseDouble(value) : 0.0; // null 대신 기본값 0.0 반환 고려
        } catch (NumberFormatException e) {
            System.err.println("parseDouble 오류: '" + value + "'는 유효한 Double 숫자가 아닙니다.");
            return 0.0; // 파싱 실패 시 0.0 반환
        }
    }

*/
}
