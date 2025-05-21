package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class DensityAreaInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    private String no;

    @Column(nullable = false)
    private String areaCd;

    private String areaNm;

    private String engNm;

    private String DensityLevel; //밀집도 레벨

    private Double latitude ; //위도

    private Double longitude; //경도
}
