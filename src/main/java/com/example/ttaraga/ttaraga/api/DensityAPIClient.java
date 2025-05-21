package com.example.ttaraga.ttaraga.api;

import com.example.ttaraga.ttaraga.dto.CityDataResponse;
import com.example.ttaraga.ttaraga.dto.Densitydto;
import kotlin.Result;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Getter
@Setter
public class DensityAPIClient {
    //private final RestTemplate restTemplate;

    @Value("${seoul.api.key}")
    private String serviceKey;
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://openapi.seoul.go.kr:8088";

    public DensityAPIClient(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }
    public CityDataResponse getCityData(String areaName) {
        try {
            String encodedAreaName = URLEncoder.encode(areaName, "UTF-8");
            String encodedServiceKey = URLEncoder.encode(serviceKey, "UTF-8");

            URI uri = UriComponentsBuilder.fromUriString(BASE_URL)
                    .pathSegment(encodedServiceKey)
                    .pathSegment("json")
                    .pathSegment("citydata")
                    .pathSegment("1")
                    .pathSegment("5")
                    .pathSegment(encodedAreaName)
                    .build()
                    .encode()
                    .toUri();

            System.out.println("API 호출: " + uri.toString());

            return restTemplate.getForObject(uri, CityDataResponse.class);

        } catch (UnsupportedEncodingException e) {
            System.err.println("URL 인코딩 실패: " + e.getMessage());
            throw new RuntimeException("URL 인코딩 오류", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("API 응답 오류 (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
            throw new RuntimeException("API 응답 오류: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            System.err.println("네트워크 연결 오류 또는 타임아웃: " + e.getMessage());
            throw new RuntimeException("네트워크 또는 타임아웃 오류", e);
        } catch (Exception e) {
            System.err.println("알 수 없는 API 호출 오류: " + e.getMessage());
            throw new RuntimeException("알 수 없는 오류 발생", e);
        }
    }

//    @JsonProperty("SeoulRtd.citydata") //실제 JSON 응답의 최상위키
//    private CityDataResponse cityDataResponse;
//
//    @Getter
//    @Setter
//    public static class SeoulRtdCitydata {
//        @JsonProperty("list_total_count")
//        private int listTotalCount;
//
//        @JsonProperty("RESULT")
//        private Result result;
//
//
//    }
//
//    @Getter
//    @Setter
//    public static class Result {
//        @JsonProperty("CODE")
//        private String code;
//        @JsonProperty("MESSAGE")
//        private String message;
//        }


//    public DensityAPIClient() {
//        this.restTemplate = new RestTemplate();
//    }
//
//    public List<Densitydto> fetchData() {
//        String baseUrl = "http://openapi.seoul.go.kr:8088";
//        String serviceKey = "7747754d61736d303935746e444267"; // 실제 키
//        String format = "json";
//        String serviceName = "citydata";
//        String startIndex = "1";
//        String endIndex = "5";
//        String area = URLEncoder.encode("광화문덕수궁", StandardCharsets.UTF_8);
//
//        String url = String.format("%s/%s/%s/%s/%s/%s/%s",
//                baseUrl, serviceKey, format, serviceName, startIndex, endIndex, area
//        );
//
//        CityDataResponse response = restTemplate.getForObject(url, CityDataResponse.class);
//        //return response != null ? response.getCityData().getRow() : List.of();
//        return null;
//    }
}
