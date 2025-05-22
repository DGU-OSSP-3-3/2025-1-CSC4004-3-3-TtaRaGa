package com.example.ttaraga.ttaraga;

import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.service.BikeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

@SpringBootApplication
@EnableScheduling // 스케줄링 활성화
public class TtaragaApplication {

    @Autowired
    private BikeService bikeService; // BikeService 주입

    public static void main(String[] args) {
        // Spring 애플리케이션 실행
        SpringApplication.run(TtaragaApplication.class, args);
    }

    @Scheduled(fixedRate = 600000) // 10분(600,000ms)마다 실행
    public void scheduleFetchAndSaveBikes() {
        System.out.println("자전거 데이터 가져오기 시작: " + java.time.LocalDateTime.now());
        Flux<BikeDto> bikeFlux = bikeService.fetchAndSaveBikes();
        bikeFlux.subscribe(
                bikeDto -> System.out.println("Station: " + bikeDto.getStationName() + ", ID: " + bikeDto.getStationId()),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("자전거 데이터 가져오기 및 저장 완료: " + java.time.LocalDateTime.now())
        );
    }
}