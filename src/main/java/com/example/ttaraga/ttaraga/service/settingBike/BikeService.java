package com.example.ttaraga.ttaraga.service.settingBike;

import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ttaraga.ttaraga.entity.Bike;
import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.api.APIClient;
import com.example.ttaraga.ttaraga.mapper.DtoMapper;
import com.example.ttaraga.ttaraga.repository.BikeRepository;

@Service
public class BikeService {
    @Autowired
    private APIClient apiClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DtoMapper dtoMapper;
    @Autowired
    private BikeRepository bikeRepository;

    @Transactional
    @Scheduled(fixedRateString = "${app.api.update-interval-ms:600000}") // ⚡️ 10분마다 실행되도록 설정
    public Flux<BikeDto> fetchAndSaveBikes() {
        int totalCount = 1471; // totalCount 고정
        return Flux.just(
                        new int[]{1, 1000}, // 첫 번째 범위: 1~1000
                        new int[]{1001, totalCount} // 두 번째 범위: 1001~1047
                )
                .concatMap(range -> {
                    System.out.println("API 호출: 범위 " + range[0] + "~" + range[1]);
                    return apiClient.fetchBikeDataJson(range[0], range[1])
                            .doOnNext(json -> System.out.println("API 응답 수신: 범위 " + range[0] + "~" + range[1]));
                })
                .flatMap(batchJson -> {
                    try {
                        JsonNode batchRoot = objectMapper.readTree(batchJson);
                        JsonNode bikeStations = batchRoot.path("rentBikeStatus").path("row");
                        if (bikeStations.isEmpty()) {
                            return Flux.error(new RuntimeException("Bike 데이터가 없습니다: " + batchJson));
                        }
                        List<BikeDto> bikeDTOs = objectMapper.convertValue(bikeStations, new TypeReference<List<BikeDto>>() {});

                          // 디버깅: BikeDto의 parkingBikeTotCnt 출력
//                        bikeDTOs.forEach(dto -> System.out.println("BikeDto: stationId=" + dto.getStationId() + ", parkingBikeTotCnt=" + dto.getParkingBikeTotCnt()));

                         // 중복 stationId 제거 및 최신 데이터 유지
                        Map<String, BikeDto> deduplicatedDTOs = bikeDTOs.stream()
                                .collect(Collectors.toMap(
                                        BikeDto::getStationId,
                                        dto -> dto,
                                        (existing, replacement) -> {
//                                            System.out.println("중복 stationId 감지: " + existing.getStationId() +
//                                                    ", 기존 parkingBikeTotCnt=" + existing.getParkingBikeTotCnt() +
//                                                    ", 새 값=" + replacement.getParkingBikeTotCnt());
                                            return replacement; // 최신 데이터로 덮어씌움
                                        }
                                ));

                        List<Bike> entities = deduplicatedDTOs.values().stream()
                                .map(dtoMapper::toBikeEntity)
                                .toList();

                        // 디버깅: Bike의 parkingBikeTotCnt 출력
//                        entities.forEach(entity -> System.out.println("Bike: stationId=" + entity.getStationId() + ", parkingBikeTotCnt=" + entity.getParkingBikeTotCnt()));

                        // 기존 데이터 확인 후 저장/업데이트
                        for (Bike entity : entities) {
                            try {
                                Bike existing = bikeRepository.findById(entity.getStationId()).orElse(null);
                                if (existing != null) {
//                                    System.out.println("기존 데이터 발견: stationId=" + existing.getStationId() +
//                                            ", 기존 parkingBikeTotCnt=" + existing.getParkingBikeTotCnt());
                                    // 기존 데이터 업데이트
                                    existing.setStationName(entity.getStationName());
                                    existing.setParkingBikeTotCnt(entity.getParkingBikeTotCnt());
                                    existing.setStationLatitude(entity.getStationLatitude());
                                    existing.setStationLongitude(entity.getStationLongitude());
                                    bikeRepository.save(existing);
//                                    System.out.println("업데이트 완료: stationId=" + existing.getStationId() +
//                                            ", parkingBikeTotCnt=" + existing.getParkingBikeTotCnt());
                                } else {
                                    // 신규 데이터 저장
                                    bikeRepository.save(entity);
//                                    System.out.println("신규 저장: stationId=" + entity.getStationId() +
//                                            ", parkingBikeTotCnt=" + entity.getParkingBikeTotCnt());
                                }
                            } catch (Exception e) {
                                System.err.println("저장 중 오류: stationId=" + entity.getStationId() + ", 오류: " + e.getMessage());
                                throw e;
                            }
                        }

                        // 저장 후 데이터베이스 조회로 확인
                        deduplicatedDTOs.keySet().forEach(stationId -> {
                            Bike saved = bikeRepository.findById(stationId).orElse(null);
                            if (saved != null) {
//                                System.out.println("DB 조회: stationId=" + stationId +
//                                        ", parkingBikeTotCnt=" + saved.getParkingBikeTotCnt());
                            } else {
                                System.out.println("DB 조회 실패: stationId=" + stationId + " 데이터 없음");
                            }
                        });

                        System.out.println("저장 완료: " + deduplicatedDTOs.size() + "개 항목");
                        return Flux.fromIterable(deduplicatedDTOs.values());
                    } catch (Exception e) {
                        return Flux.error(new RuntimeException("JSON 파싱 실패: " + e.getMessage(), e));
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("오류 발생: " + e.getMessage());
                    return Flux.error(e);
                });
    }

    // Entity를 DTO로 변환하는 헬퍼 메서드
    private BikeDto convertToDto(Bike bike) {
        BikeDto dto = new BikeDto();
        dto.setStationId(bike.getStationId());
        dto.setParkingBikeTotCnt(bike.getParkingBikeTotCnt());
        dto.setStationLatitude(bike.getStationLatitude());
        dto.setStationLongitude(bike.getStationLongitude());
        dto.setStationName(bike.getStationName());
        return dto;
    }

    // DB에서 모든 Bike entity 조회.
    public List<BikeDto> getAllBikes () {
        List<Bike> bikes = bikeRepository.findAll(); // findAll()은 JpaRepository의 기본 메서드
        return bikes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BikeDto getBikeStationById(Long id) {
        // ID로 Bike 엔티티를 찾아서 DTO로 변환
        return bikeRepository.findById(String.valueOf(id))
                .map(this::convertToDto)
                .orElse(null);
    }
}

