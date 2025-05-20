package com.example.ttaraga.ttaraga.service;

import com.example.ttaraga.ttaraga.api.APIClient;
import com.example.ttaraga.ttaraga.mapper.DtoMapper;
import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.entity.Bike;
import com.example.ttaraga.ttaraga.repository.BikeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.List;

@Service
public class BikeService {
    private final APIClient apiClient;
    private final BikeRepository bikeRepository;
    private final DtoMapper dtoMapper;
    private final ObjectMapper objectMapper;

    public BikeService(APIClient apiClient, BikeRepository bikeRepository, DtoMapper dtoMapper, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.bikeRepository = bikeRepository;
        this.dtoMapper = dtoMapper;
        this.objectMapper = objectMapper;
    }

    public Flux<BikeDto> fetchAndSaveBikes() {
        return apiClient.fetchBikeDataJson(1, 5)
                .flatMapMany(json -> {
                    try {
                        JsonNode root = objectMapper.readTree(json);
                        int totalCount = root.path("rentBikeStatus").path("list_total_count").asInt(1471);
                        return Flux.just(
                                        new int[]{1, Math.min(1000, totalCount)},
                                        new int[]{1001, Math.min(1471, totalCount)}
                                )
                                .flatMap(range -> apiClient.fetchBikeDataJson(range[0], range[1]))
                                .flatMap(batchJson -> {
                                    try {
                                        JsonNode batchRoot = objectMapper.readTree(batchJson);
                                        JsonNode bikeStations = batchRoot.path("rentBikeStatus").path("row");
                                        List<BikeDto> bikeDTOs = objectMapper.convertValue(bikeStations, new TypeReference<List<BikeDto>>() {});
                                        List<Bike> entities = bikeDTOs.stream()
                                                .map(dtoMapper::toBikeEntity)
                                                .toList();
                                        bikeRepository.saveAll(entities);
                                        return Flux.fromIterable(bikeDTOs);
                                    } catch (Exception e) {
                                        return Flux.error(new RuntimeException("Batch JSON 파싱 실패: " + e.getMessage()));
                                    }
                                });
                    } catch (Exception e) {
                        return Flux.error(new RuntimeException("초기 JSON 파싱 실패: " + e.getMessage()));
                    }
                });
    }

    public Flux<BikeDto> getAllBikes() {
        return Flux.fromIterable(bikeRepository.findAll())
                .map(dtoMapper::toBikeDto);
    }
}