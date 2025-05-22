package com.example.ttaraga.ttaraga.mapper;

import com.example.ttaraga.ttaraga.dto.AttractingPlaceDto;
import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.dto.DensityDto;
import com.example.ttaraga.ttaraga.entity.AttractingPlace;
import com.example.ttaraga.ttaraga.entity.Bike;
import com.example.ttaraga.ttaraga.entity.Density;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    // BikeDto -> Bike Entity
    public Bike toBikeEntity(BikeDto bikeDto) {
        Bike bike = new Bike();
        bike.setStationId(bikeDto.getStationId());
        bike.setStationName(bikeDto.getStationName());
        bike.setStationLatitude(bikeDto.getStationLatitude());
        bike.setStationLongitude(bikeDto.getStationLongitude());
        bike.setParkingBikeTotCnt(bikeDto.getParkingBikeTotCnt());
        return bike;
    }

    // Bike Entity -> BikeDto
    public BikeDto toBikeDto(Bike bike) {
        BikeDto bikeDto = new BikeDto();
        bikeDto.setStationId(bike.getStationId());
        bikeDto.setStationName(bike.getStationName());
        bikeDto.setStationLatitude(bike.getStationLatitude());
        bikeDto.setStationLongitude(bike.getStationLongitude());
        return bikeDto;
    }

    // DensityDto -> Density Entity
    public Density toDensityEntity(DensityDto densityDto) {
        Density density = new Density();
        density.setAREA_NM(densityDto.getAREA_NM());
        density.setAREA_CD(densityDto.getAREA_CD());
        density.setLatitude(densityDto.getLatitude());
        density.setLongitude(densityDto.getLongitude());
        density.setAREA_CONGEST_LVL(densityDto.getAREA_CONGEST_LVL());
        return density;
    }

    // Density Entity -> DensityDto
    public DensityDto toDensityDto(Density density) {
        DensityDto densityDto = new DensityDto();
        densityDto.setAREA_NM(density.getAREA_NM());
        densityDto.setAREA_CD(density.getAREA_CD());
        densityDto.setLatitude(density.getLatitude());
        densityDto.setLongitude(density.getLongitude());
        densityDto.setAREA_CONGEST_LVL(density.getAREA_CONGEST_LVL());
        return densityDto;
    }

    // AttractingPlaceDto -> AttractingPlace Entity
    public AttractingPlace toAttractingPlaceEntity(AttractingPlaceDto dto) {
        AttractingPlace place = new AttractingPlace();
        // **이 줄을 추가해야 합니다.**
        place.setPlaceId(dto.getPlaceId()); // DTO에서 placeId를 엔티티로 복사

        place.setPlaceName(dto.getPlaceName());
        place.setPlaceLatitude(dto.getPlaceLatitude());
        place.setPlaceLongitude(dto.getPlaceLongitude());
        place.setDetail(dto.getDetail());
        return place;
    }

    // AttractingPlace Entity -> AttractingPlaceDto
    public AttractingPlaceDto toAttractingPlaceDto(AttractingPlace place) {
        AttractingPlaceDto dto = new AttractingPlaceDto();
        // 이 줄도 추가하면 좋습니다. (엔티티에서 DTO로 변환 시 placeId도 복사)
        dto.setPlaceId(place.getPlaceId()); // 엔티티에서 placeId를 DTO로 복사

        dto.setPlaceName(place.getPlaceName());
        dto.setPlaceLatitude(place.getPlaceLatitude());
        dto.setPlaceLongitude(place.getPlaceLongitude());
        dto.setDetail(place.getDetail());
        return dto;
    }
}