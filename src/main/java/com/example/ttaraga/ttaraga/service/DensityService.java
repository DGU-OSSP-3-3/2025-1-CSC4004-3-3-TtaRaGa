package com.example.ttaraga.ttaraga.service;

import com.example.ttaraga.ttaraga.mapper.DtoMapper;
import com.example.ttaraga.ttaraga.dto.DensityDto;
import com.example.ttaraga.ttaraga.entity.Density;
import com.example.ttaraga.ttaraga.repository.DensityRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.List;

@Service
public class DensityService {
    private final ApiClient apiClient;
    private final DensityRepository densityRepository;
    private final DtoMapper dtoMapper;
    private final ObjectMapper objectMapper;

    public DensityService(ApiClient apiClient, DensityRepository densityRepository, DtoMapper dtoMapper, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.densityRepository = densityRepository;
        this.dtoMapper = dtoMapper;
        this.objectMapper = objectMapper;
    }

    public Flux<DensityDto> fetchAndSaveDensities() {
        return apiClient.fetchDensityJson()
                .flatMapMany(json -> {
                    try {
                        List<DensityDto> densityDTOs = objectMapper.readValue(json, new TypeReference<List<DensityDto>>(){});
                        List<Density> entities = densityDTOs.stream()
                                .map(dtoMapper::toDensityEntity)
                                .toList();
                        densityRepository.saveAll(entities);
                        return Flux.fromIterable(densityDTOs);
                    } catch (Exception e) {
                        return Flux.error(new RuntimeException("Density JSON 파싱 실패: " + e.getMessage()));
                    }
                });
    }

    public Flux<DensityDto> getAllDensities() {
        return Flux.fromIterable(densityRepository.findAll())
                .map(dtoMapper::toDensityDto);
    }
}

