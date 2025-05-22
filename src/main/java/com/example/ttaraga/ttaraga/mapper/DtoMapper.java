package com.example.ttaraga.ttaraga.mapper;

import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.dto.DensityDto;
import com.example.ttaraga.ttaraga.entity.Bike;
import com.example.ttaraga.ttaraga.entity.Density;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    // BikeDto -> Entity
    public Bike toBikeEntity(BikeDto bikedto){
        Bike bike = new Bike();
        bike.setStationId(bikedto.getStationId());
        bike.setStationName(bikedto.getStationName());
        bike.setStationLatitude(bikedto.getStationLatitude());
        bike.setStationLongitude(bikedto.getStationLongitude());
        bike.setParkingBikeTotCnt(bikedto.getParkingBikeTotCnt());
        return bike;
    }

    //Bike Entity -> Dto
    public BikeDto toBikeDto(Bike bike) {
        BikeDto bikeDto = new BikeDto();
        bikeDto.setStationId(bike.getStationId());
        bikeDto.setStationName(bike.getStationName());
        bikeDto.setStationLatitude(bike.getStationLatitude());
        bikeDto.setStationLongitude(bike.getStationLongitude());
        return bikeDto;
    }

    // DensityDto -> Entity
    public Density toDensityEntity(DensityDto densityDto){
        Density density = new Density();
        density.setAREA_NM(densityDto.getAREA_NM());
        density.setAREA_CD(densityDto.getAREA_CD());
        density.setLatitude(densityDto.getLatitude());
        density.setLongitude(densityDto.getLongitude());
        density.setAREA_CONGEST_LVL(densityDto.getAREA_CONGEST_LVL());
        return density;
    }

    // Density Entity -> Dto
    public DensityDto toDensityDto(Density density) {
        DensityDto densityDto = new DensityDto();
        densityDto.setAREA_NM(density.getAREA_NM());
        densityDto.setAREA_CD(density.getAREA_CD());
        densityDto.setLatitude(density.getLatitude());
        densityDto.setLongitude(density.getLongitude());
        densityDto.setAREA_CONGEST_LVL(densityDto.getAREA_CONGEST_LVL());
        return densityDto;
    }
}
