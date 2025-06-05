package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
서울 매력, 동행 엔티티
 */

@Entity
@Getter
@Setter
public class AttractingPlace {
    @Id
    @Column(name="place_id", nullable=false)
    private String placeId;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "place_latitude", nullable = false)
    private double placeLatitude;

    @Column(name ="place_longitude", nullable = false)
    private double placeLongitude;

    @Column(name = "detail", nullable = false)
    private String detail;
}