package com.example.ttaraga.ttaraga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/*
서울 실시간 밀집도 엔티티
 */

@Getter
@Setter
@Entity
public class DensityAreaInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //식별자 .

    private String category; // 지역 특징  예_ 상권, 공원 등등

    private String no; //지역 넘버

    @Column(nullable = false)
    private String areaCd; //코드 , 식별자 2

    private String areaNm; //밀집도 지역 이름

    private String engNm; //영어이름

    private String DensityLevel; //밀집도 레벨

    private Double latitude ; //위도

    private Double longitude; //경도
}
