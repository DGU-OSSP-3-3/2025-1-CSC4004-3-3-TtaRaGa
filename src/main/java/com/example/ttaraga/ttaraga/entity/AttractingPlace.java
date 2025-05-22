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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long j;

    @Column(nullable = false)
    private double pointLatitude;

    @Column(nullable = false)
    private double pointLongitude;

    @Column(name = "pointName", nullable = false)
    private String pointName;
}