package com.example.ttaraga.ttaraga.service;

import com.example.ttaraga.ttaraga.mapper.DtoMapper;
import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.entity.Bike;
import com.example.ttaraga.ttaraga.repository.BikeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.List;

@Service
public class BikeService {
    private final ApiClient apiClient;
    private final BikeRepository bikeRepository;
    private final DtoMapper dtoMapper;
    private final ObjectMapper objectMapper;

    public BikeService(ApiClient apiClient, BikeRepository bikeRepository, DtoMapper dtoMapper, ObjectMapper objectMapper) {
        this.apiClient = apiClient;  // 외부 API 호출
        this.bikeRepository = bikeRepository;  // JPA 리포지토리로 DB 작업
        this.dtoMapper = dtoMapper;  // DTO와 Entity 변환
        this.objectMapper = objectMapper;  // JSON을 객체로 파싱.
    }

    // ApiClient에서 JSON 데이터를 가져와 BikeDto 리스트로 파싱.
    public Flux<BikeDto> fetchAndSaveBikes() {
        return apiClient.fetchBikesJson()
                .flatMapMany(json -> {
                    try {
                        List<BikeDto> bikeDTOs = objectMapper.readValue(json, new TypeReference<List<BikeDto>>() {
                        });
                        List<Bike> entities = bikeDTOs.stream()
                                .map(dtoMapper::toBikeEntity)
                                .toList();
                        bikeRepository.saveAll(entities);
                        return Flux.fromIterable(bikeDTOs);
                    } catch (Exception e) {
                        return Flux.error(new RuntimeException("Bike JSON 파싱 실패: " + e.getMessage()));
                    }
                });
    }
        // DB에서 모든 Bike entity 조회.
        public Flux<BikeDto> getAllBikes (){
            return Flux.fromIterable(bikeRepository.findAll())
                    .map(dtoMapper::toBikeDto);
        }
}
