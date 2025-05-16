package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AttractingPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long midpointId;

    @Column(nullable = false)
    private double pointLatitude;

    @Column(nullable = false)
    private double pointLongitude;

    @Column(name = "pointName", nullable = false)
    private String pointName;
}