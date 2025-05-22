package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Density {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long AREA_CD;

    @Column(nullable = false)
    private String AREA_NM;

    @Column
    private double latitude;

    @Column
    private double longitude;

    @Column(name = "AREA_CONGEST_LVL", nullable = false)
    private String AREA_CONGEST_LVL;
}

