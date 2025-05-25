package com.example.ttaraga.ttaraga.dto;

import jakarta.persistence.Column;
import lombok.Data;
import com.example.ttaraga.ttaraga.utility.StringToLongDeserializer;

@Data
public class AccInfoDto {

    private String acc_id;
    private String occr_time;
    private String acc_info;
    private double acc_latitude;
    private double acc_longitude;
}
