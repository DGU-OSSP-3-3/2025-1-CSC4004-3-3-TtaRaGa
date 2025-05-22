package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AttractingPlace {
    @Id
    private String placeId;

    @Column(name = "placeName", nullable = false)
    private String placeName;

    @Column(nullable = false)
    private double placeLatitude;

    @Column(nullable = false)
    private double placeLongitude;

    @Column(name = "detail", nullable = false)
    private String detail;
}