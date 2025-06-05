package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Bike {
    @Id
    @Column(name = "station_id", nullable = false)
    private String stationId;

    @Column(name = "parking_bike_tot_cnt", nullable = false)
    private long parkingBikeTotCnt;

    @Column(name = "station_latitude", nullable = false)
    private double stationLatitude;

    @Column(name = "station_longitude", nullable = false)
    private double stationLongitude;

    @Column(name = "station_name", nullable = false)
    private String stationName;
}

