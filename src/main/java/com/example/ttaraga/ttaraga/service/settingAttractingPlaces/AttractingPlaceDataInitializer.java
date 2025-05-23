package com.example.ttaraga.ttaraga.service.settingAttractingPlaces;

import com.example.ttaraga.ttaraga.entity.AttractingPlace;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component // Spring Bean으로 등록하여 컨테이너가 관리하도록 함
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입
public class AttractingPlaceDataInitializer {

    private final AttractingPlaceService attractingPlaceService; // AttractingPlaceService 주입

    @PostConstruct // 애플리케이션 시작 시 이 메서드가 자동으로 실행됨
    public void initializeAttractingPlaces() { // 메서드 이름은 init() 또는 initialize... 로 명확하게
        System.out.println("명소 데이터 초기화 시작: " + java.time.LocalDateTime.now());
        try {
            ClassPathResource resource = new ClassPathResource("AttractingPlace.xlsx");
            if (!resource.exists()) {
                System.err.println("오류: 엑셀 파일이 존재하지 않습니다. 경로: " + resource.getPath());
                System.err.println("파일이 'src/main/resources/AttractingPlace.xlsx'에 있는지 확인해주세요.");
                return;
            }

            MultipartFile multipartFile = new MockMultipartFile(
                    "AttractingPlace.xlsx",
                    "AttractingPlace.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    resource.getInputStream()
            );

            // AttractingPlaceService를 통해 엑셀 데이터 저장 로직 호출
            List<AttractingPlace> savedPlaces = attractingPlaceService.savePlacesFromExcel(multipartFile);
            System.out.println("명소 데이터 초기화 완료: " + savedPlaces.size() + "개 저장됨, 시간: " + java.time.LocalDateTime.now());
        } catch (IOException e) {
            System.err.println("엑셀 파일 처리 중 IO 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("명소 데이터 초기화 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}