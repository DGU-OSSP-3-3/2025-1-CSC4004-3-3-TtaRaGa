package com.example.ttaraga.ttaraga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BikeData {
    @JsonProperty("row")
    private List<Bikedto> row;
}
