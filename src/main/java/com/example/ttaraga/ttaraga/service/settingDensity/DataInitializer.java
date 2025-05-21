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
