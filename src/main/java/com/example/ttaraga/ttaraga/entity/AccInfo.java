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

    @Column
    private String occr_time;

    @Column(nullable=false)
    private String acc_info;

    @Column(nullable = false)
    private double acc_latitude;

    @Column(nullable = false)
    private double acc_longitude;
}