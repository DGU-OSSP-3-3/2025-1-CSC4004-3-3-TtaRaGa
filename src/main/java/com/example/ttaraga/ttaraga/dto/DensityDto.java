package com.example.ttaraga.ttaraga.dto;

import lombok.Data;

//혼잡도 데이터 관리를 위한 Dto
@Data
public class DensityDto {
    private Long id;
    private double latitude;
    private double longitude;
    private int densityLevel;
}
