package com.example.ttaraga.ttaraga.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.net.URL;
import java.net.URLEncoder;

@Component
public class ApiClient {
    private final WebClient webClient;

    public ApiClient(WebClient.Builder webClientBuilder) {
        StringBuilder urlBuilder = new StringBuilder("http://openapi.seoul.go.kr:8088");
        urlBuilder.append("/" + URLEncoder.encode("7747754d61736d303935746e444267", "UTF-8")); // 인증키
        urlBuilder.append("/" + URLEncoder.encode("json", "UTF-8")); // 요청파일타입
        urlBuilder.append("/" + URLEncoder.encode("citydata", "UTF-8")); // 서비스명
        urlBuilder.append("/" + URLEncoder.encode("1", "UTF-8")); // 요청시작위치
        urlBuilder.append("/" + URLEncoder.encode("5", "UTF-8")); // 요청종료위치
        urlBuilder.append("/" + URLEncoder.encode("광화문·덕수궁", "UTF-8")); // 지역명

        URL url = new URL(urlBuilder.toString());
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    public Mono<String> fetchBikesJson() {
        return webClient.get()
                .uri("/bikes")
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> fetchDensityJson() {
        return webClient.get()
                .uri("/densities")
                .retrieve()
                .bodyToMono(String.class);
    }
}

