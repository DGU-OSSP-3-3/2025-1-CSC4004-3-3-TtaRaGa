package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; //db의 고유 PK

    @Column(unique = true, nullable = false)
    private String apiStationId;

    @Column(nullable = false)
    private long parkingBikeTotCnt; //거치대에 있는 자전거 수

    @Column(nullable = false)
    private double stationLatitude; //위도

    @Column(nullable = false)
    private double stationLongitude; //경도

    @Column(name = "stationName", nullable = false)
    private String stationName; // 대여소 이름

    // toString() 메서드 업데이트
    @Override
    public String toString() {
        return "Bike{" +
                "id=" + id +
                ", apiStationId='" + apiStationId + '\'' +
                ", stationName='" + stationName + '\'' +
                ", parkingBikeTotCnt=" + parkingBikeTotCnt +
                ", stationLatitude=" + stationLatitude +
                ", stationLongitude=" + stationLongitude +
                '}';
    }
}

