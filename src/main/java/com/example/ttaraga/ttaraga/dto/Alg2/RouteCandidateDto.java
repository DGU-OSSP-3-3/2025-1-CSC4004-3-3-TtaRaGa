package com.example.ttaraga.ttaraga.dto.Alg2;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Agl2에서 사용됨
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteCandidateDto {
    private String point1_id;
    private String point1_name;
    private double point1_lat;
    private double point1_lon;
    private String point2_id;
    private String point2_name;
    private double point2_lat;
    private double point2_lon;
    private double total_distance_m;
}
