package com.example.ttaraga.ttaraga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling // 스케줄링 활성화
public class TtaragaApplication {

    public static void main(String[] args) {
        // Spring 애플리케이션 실행
        SpringApplication.run(TtaragaApplication.class, args);
    }

}