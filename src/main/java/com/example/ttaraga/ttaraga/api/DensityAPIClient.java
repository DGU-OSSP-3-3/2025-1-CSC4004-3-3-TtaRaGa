package com.example.ttaraga.ttaraga.api;

import com.example.ttaraga.ttaraga.dto.CityDataResponse;
import com.example.ttaraga.ttaraga.dto.Densitydto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class DensityAPIClient {
    private final RestTemplate restTemplate;

    public DensityAPIClient() {
        this.restTemplate = new RestTemplate();
    }

    public List<Densitydto> fetchData() {
        String baseUrl = "http://openapi.seoul.go.kr:8088";
        String serviceKey = "7747754d61736d303935746e444267"; // 실제 키
        String format = "json";
        String serviceName = "citydata";
        String startIndex = "1";
        String endIndex = "5";
        String area = URLEncoder.encode("광화문덕수궁", StandardCharsets.UTF_8);

        String url = String.format("%s/%s/%s/%s/%s/%s/%s",
                baseUrl, serviceKey, format, serviceName, startIndex, endIndex, area
        );

        CityDataResponse response = restTemplate.getForObject(url, CityDataResponse.class);
        return response != null ? response.getCityData().getRow() : List.of();
    }
}
