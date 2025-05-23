package com.example.ttaraga.ttaraga.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class APIClient {
    private final WebClient webClient;

    @Value("${seoul.api.bike}")
    private String apiKey;

    private final String baseUrl = "http://openapi.seoul.go.kr:8088";

    public APIClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<String> fetchBikeDataJson(int startIndex, int endIndex) {
        String uri = String.format("/%s/json/bikeList/%d/%d",
                apiKey, startIndex, endIndex);
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("API 호출 실패: " + e.getMessage())));
    }
    public Mono<String> fetchDensityDataJson(String areaName, int startIndex, int endIndex) {
        String uri = String.format("/%s/json/citydata/%d/%d/%s",
                apiKey, startIndex, endIndex, encodeAreaName(areaName));
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("API 호출 실패: " + e.getMessage())));
    }

    private String encodeAreaName(String areaName) {
        try {
            return java.net.URLEncoder.encode(areaName, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("지역명 인코딩 실패: " + areaName, e);
        }
    }
}