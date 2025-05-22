package com.example.ttaraga.ttaraga;

import com.example.ttaraga.ttaraga.dto.BikeDto;
import com.example.ttaraga.ttaraga.entity.AttractingPlace;
import com.example.ttaraga.ttaraga.service.AttractingPlaceService;
import com.example.ttaraga.ttaraga.service.BikeService;
import org.springframework.mock.web.MockMultipartFile;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SpringBootApplication
@EnableScheduling // 스케줄링 활성화
public class TtaragaApplication {

//    @Autowired
//    private BikeService bikeService; // BikeService 주입

    @Autowired
    private AttractingPlaceService attractingPlaceService; // 명소 데이터 서비스 주입

    public static void main(String[] args) {
        // Spring 애플리케이션 실행
        SpringApplication.run(TtaragaApplication.class, args);
    }

//    @Scheduled(fixedRate = 600000) // 10분(600,000ms)마다 실행
//    public void scheduleFetchAndSaveBikes() {
//        System.out.println("자전거 데이터 가져오기 시작: " + java.time.LocalDateTime.now());
//        Flux<BikeDto> bikeFlux = bikeService.fetchAndSaveBikes();
//        bikeFlux.subscribe(
//                bikeDto -> System.out.println("Station: " + bikeDto.getStationName() + ", ID: " + bikeDto.getStationId()),
//                error -> System.err.println("Error: " + error.getMessage()),
//                () -> System.out.println("자전거 데이터 가져오기 및 저장 완료: " + java.time.LocalDateTime.now())
//        );
//    }

    @PostConstruct
    public void initPlaces() {
        System.out.println("명소 데이터 가져오기 시작: " + java.time.LocalDateTime.now());
        try {
            // resources 디렉토리에서 엑셀 파일 로드
            ClassPathResource resource = new ClassPathResource("AttractingPlace.xlsx");
            if (!resource.exists()) {
                System.err.println("엑셀 파일이 존재하지 않습니다: " + resource.getPath());
                return;
            }

            // ClassPathResource를 MultipartFile로 변환
            MultipartFile multipartFile = new MockMultipartFile(
                    "AttractingPlace.xlsx",
                    "AttractingPlace.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    resource.getInputStream()
            );

            List<AttractingPlace> savedPlaces = attractingPlaceService.savePlacesFromExcel(multipartFile);
            System.out.println("명소 데이터 저장 완료: " + savedPlaces.size() + "개, 시간: " + java.time.LocalDateTime.now());
        } catch (IOException e) {
            System.err.println("엑셀 파일 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}