package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Bike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stationId;

    @Column(nullable = false)
    private long parkingBikeTotCnt;

    @Column(nullable = false)
    private double stationLatitude;

    @Column(nullable = false)
    private double stationLongitude;

    @Column(name = "stationName", nullable = false)
    private String stationName;
}

