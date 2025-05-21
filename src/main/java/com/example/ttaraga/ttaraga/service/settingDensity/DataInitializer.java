package com.example.ttaraga.ttaraga.service.settingDensity;
/*
엑셀 데이터(인구밀집도 도시 이름 리스트) 를 db에 넣는 과정
 */
import com.example.ttaraga.ttaraga.entity.DensityAreaInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final ExcelReaderService excelReaderService;
    private final DensityAreaInfoService areaInfoService;
    private final DensityService densityService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("엑셀 데이터 로드하기");

        try (InputStream is = getClass().getResourceAsStream("/DensityAreaInfo.xlsx")) {
            if (is == null) {
                throw new IllegalArgumentException("Excel 파일을 찾을 수 없습니다.");
            }
            List<DensityAreaInfo> areaInfos = excelReaderService.readAreaInfoFromExcel(is);
            areaInfoService.saveAll(areaInfos);
            System.out.println("엑셀 데이터를 DB에 저장 완료, 총 개수: " + areaInfos.size());

//            if (areaInfoService.count() == 0) {
//
//            } else {
//                System.out.println("이미 데이터가 존재하므로 저장을 건너뜁니다.");
//            }
        } catch (Exception e) {
            System.err.println("엑셀 파일 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

        // 2. 엑셀 데이터 로드 완료 후, API를 통해 인구 밀집도 레벨 업데이트 (애플리케이션 시작 시 한 번)
        //    areaInfoService.count() > 0 이라는 것은 엑셀 데이터가 DB에 있음을 의미합니다.
        if (areaInfoService.count() > 0) {
            try {
                System.out.println("--- 애플리케이션 시작 시 인구 밀집도 레벨 API 호출 및 DB 업데이트 시작 ---");
                // 스케줄링된 메서드를 직접 호출하여 즉시 첫 업데이트를 수행합니다.
                densityService.updatePopulationDensityLevelsFromApi();
                System.out.println("--- 애플리케이션 시작 시 인구 밀집도 레벨 API 호출 및 DB 업데이트 완료 ---");
            } catch (Exception e) {
                System.err.println("애플리케이션 시작 시 인구 밀집도 레벨 업데이트 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("엑셀 데이터 로드 실패 또는 데이터 없음. API 업데이트를 건너뜁니다.");
        }

        System.out.println("--- 애플리케이션 초기화 완료 ---");
    }
}
//@Component
//@RequiredArgsConstructor

//public class DataInitializer {
//    private final ExcelReaderService excelReaderService;
//    private final DensityAreaInfoService areaInfoService;
//
//
//
//    @Bean
//    public ApplicationRunner loadData() {
//        System.out.println("엑셀 데이터 로드하기 ");
//        return args -> {
//            try (InputStream is = getClass().getResourceAsStream("C:\\DDDDD\\src\\main\\resources\\DensityAreaInfo.xlsx")) {
//                if (is == null) {
//                    throw new IllegalArgumentException("Excel 파일을 찾을 수 없습니다.");
//                }
//                // DB에 데이터가 없을 경우에만 저장
//                if (areaInfoService.count() == 0) {
//                    List<DensityAreaInfo> areaInfos = excelReaderService.readAreaInfoFromExcel(is);
//                    areaInfoService.saveAll(areaInfos);
//                    System.out.println("엑셀 데이터를 DB에 저장 완료, 총 개수: " + areaInfos.size());
//                } else {
//                    System.out.println("이미 데이터가 존재하므로 저장을 건너뜁니다.");
//                }
//
//            } catch (Exception e) {
//                System.err.println("엑셀 파일 로딩 중 오류 발생: " + e.getMessage());
//                e.printStackTrace();
//            }
//        };
//
//    }
//
//}
