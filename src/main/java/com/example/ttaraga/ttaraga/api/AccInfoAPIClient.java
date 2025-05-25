package com.example.ttaraga.ttaraga.api;

import com.example.ttaraga.ttaraga.mapper.DtoMapper;
import com.example.ttaraga.ttaraga.repository.AccInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AccInfoAPIClient {
    private final WebClient webClient;

    @Value("${seoul.api.key}")
    private String key;

    private final String baseUrl = "http://openapi.seoul.go.kr:8088";

    AccInfoAPIClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<String> fetchAccInfo(int startIndex, int endIndex) {
        String uri = String.format("/%s/xml/AccInfo/%d/%d",
                key, startIndex, endIndex);
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("API 호출 실패: " + e.getMessage())));
    }
}
