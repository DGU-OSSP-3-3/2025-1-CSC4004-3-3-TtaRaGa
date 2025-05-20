package com.example.ttaraga.ttaraga;

import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.service.BikeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class TtaragaApplication {
    public static void main(String[] args) {
        // Spring 애플리케이션 실행 및 컨텍스트 가져오기
        ApplicationContext context = SpringApplication.run(TtaragaApplication.class, args);

        // BikeService 빈 가져오기
        BikeService bikeService = context.getBean(BikeService.class);

        // API 호출 및 결과 출력
        Flux<BikeDto> bikeFlux = bikeService.fetchAndSaveBikes();
        bikeFlux.subscribe(
                bikeDto -> System.out.println("Station: " + bikeDto.getStationName() + ", ID: " + bikeDto.getStationId()),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("API 호출 완료")
        );

        // 비동기 호출이 완료될 때까지 대기 (테스트용, 프로덕션에서는 제거)
        try {
            Thread.sleep(5000); // 5초 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}