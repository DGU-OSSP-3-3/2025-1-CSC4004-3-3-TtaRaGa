package com.example.ttaraga.ttaraga.mapper;

import com.example.ttaraga.ttaraga.dto.AttractingPlaceDto;
import com.example.ttaraga.ttaraga.entity.AttractingPlace;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    // AttractingPlaceDto -> AttractingPlace Entity
    public AttractingPlace toAttractingPlaceEntity(AttractingPlaceDto dto) {
        AttractingPlace place = new AttractingPlace();
        place.setPlaceId(dto.getPlaceId());

        place.setPlaceName(dto.getPlaceName());
        place.setPlaceLatitude(dto.getPlaceLatitude());
        place.setPlaceLongitude(dto.getPlaceLongitude());
        place.setDetail(dto.getDetail());
        return place;
    }

    // AttractingPlace Entity -> AttractingPlaceDto
    public AttractingPlaceDto toAttractingPlaceDto(AttractingPlace place) {
        AttractingPlaceDto dto = new AttractingPlaceDto();
        dto.setPlaceId(place.getPlaceId()); // 엔티티에서 placeId를 DTO로 복사

        dto.setPlaceName(place.getPlaceName());
        dto.setPlaceLatitude(place.getPlaceLatitude());
        dto.setPlaceLongitude(place.getPlaceLongitude());
        dto.setDetail(place.getDetail());
        return dto;
    }
}