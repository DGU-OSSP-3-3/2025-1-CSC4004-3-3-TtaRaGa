package com.example.ttaraga.ttaraga.mapper;

import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.entity.Bike;
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


}
