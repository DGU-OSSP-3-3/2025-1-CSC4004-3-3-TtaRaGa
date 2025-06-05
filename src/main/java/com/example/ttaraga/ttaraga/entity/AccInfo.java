package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
서울 실시간 돌발(사고) 정보 엔티티
 */

@Entity
@Getter
@Setter
public class AccInfo {
    @Id
    @Column(name = "acc_id", nullable = false)
    private String acc_id;

    @Column(name = "acc_time", nullable = false)
    private String occr_time;

    @Column(name = "acc_info", nullable=false)
    private String acc_info;

    @Column(name = "acc_latitude", nullable = false)
    private double acc_latitude;

    @Column(name = "acc_longitude", nullable = false)
    private double acc_longitude;
}