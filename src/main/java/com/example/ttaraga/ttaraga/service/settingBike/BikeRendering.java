package com.example.ttaraga.ttaraga.service.settingBike;

import com.example.ttaraga.ttaraga.dto.BikeResponseDto;
import com.example.ttaraga.ttaraga.repository.BikeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BikeRendering {
    private final BikeRepository bikeRepository;

    public BikeRendering(BikeRepository bikeRepository) {
        this.bikeRepository = bikeRepository;
    }

    public BikeResponseDto getStationInBounds(Double lat1, Double lng1, Double delta1, Double delta2) {
        if(lat1 == null || lng1 == null || delta1 == null || delta2 == null ||
           lat1 < -90 || lat1 > 90 || lng1 < -180 || lng1 > 180 ||
           lat1 + delta1 < -90 || lat1 + delta1 > 90 ||
           lng1 + delta1 < -180 || lng1 + delta1 > 180) {
            throw new IllegalArgumentException("유효하지 않은 좌표 또는 델타 값입니다");
        }

        List<Object[]> results = bikeRepository.findStationsInBounds(lat1, lng1, delta1, delta2);

        // Object[]를 Dto로 변환
        List<BikeResponseDto.BikeDto> stations = results.stream()
                .map(row -> new BikeResponseDto.BikeDto(
                        (String) row[0],                  // stationId
                        ((Number) row[1]).longValue(),    // parkingBikeTotCnt
                        (Double) row[2],                  // stationLatitude
                        (Double) row[3],                  // stationLongitude
                        (String) row[4]                   // stationName
                ))
                .collect(Collectors.toList());

        // station_count는 모든 레코드에서 동일하므로 첫 번째 레코드에서 추출.
        Long stationCount = results.isEmpty()? 0L: ((Number) results.get(0)[5]).longValue();

        return new BikeResponseDto(stationCount, stations);
    }
}
