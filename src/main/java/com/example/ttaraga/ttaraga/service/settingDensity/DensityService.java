package com.example.ttaraga.ttaraga.service.settingDensity;


import com.example.ttaraga.ttaraga.api.DensityNewAPIClient;
import com.example.ttaraga.ttaraga.dto.CityDataResponse_NEW;
import com.example.ttaraga.ttaraga.entity.DensityAreaInfo;
import com.example.ttaraga.ttaraga.repository.DensityAreaInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/*
1. 인구 밀집도 엑셀 데이터를 db에 로드하고 (코드 실행 시 맨 처음 1회만 실행)
2. 인구 밀집도 api를 가져와서 맨 처음 시작 실행 + 10분 단위로 반복 실행 1

api를 불러오는데 걸리는 시간이 대략 2~3분 걸림
다 불러오고 내용을 리스트에 담은 다음에 한번에 db에 넣어줌 (이건 1초도 안걸림)

학교 와파 왜이래
 */

@Service
@RequiredArgsConstructor
public class DensityService {

    private final DensityAreaInfoRepository densityAreaInfoRepository;
    private final DensityNewAPIClient densityNewAPIClient;

    @Scheduled(fixedRateString = "${app.api.update-interval-ms:600000}") // ⚡️ 10분마다 실행되도록 설정
    @Transactional // 데이터 변경 작업을 위해 트랜잭션 적용
    public void updatePopulationDensityLevelsFromApi() {
        System.out.println("API를 통한 인구 밀집도 레벨 업데이트 시작...");

        // 1. DB에서 기존에 저장된 모든 지역 정보 (areaNm)를 가져옵니다.
        //    이 데이터는 엑셀 파일에서 로드된 초기 데이터라고 가정합니다.
        List<DensityAreaInfo> allAreasInDb = densityAreaInfoRepository.findAll();
        int updatedCount = 0;

        if (allAreasInDb.isEmpty()) {
            System.out.println("DB에 업데이트할 지역 정보가 없습니다. 엑셀 데이터 로드 여부를 확인해주세요.");
            return;
        }

        for (DensityAreaInfo areaInfo : allAreasInDb) {
            String areaNm = areaInfo.getAreaNm();

            if (areaNm == null || areaNm.trim().isEmpty()) {
                System.err.println("경고: areaNm이 비어있어 API 호출을 건너뜜. ID: " + areaInfo.getId());
                continue;
            }

            try {
                // 2. 각 지역명(areaNm)으로 API를 호출하여 최신 인구 밀집도 레벨을 가져옵니다.
                String densityLevel = densityNewAPIClient.getPopulationDensityLevel(areaNm); // ✅ 변경된 클라이언트 호출

                if (densityLevel != null) {
                    // 3. API에서 가져온 최신 밀집도 레벨로 엔티티 업데이트
                    areaInfo.setDensityLevel(densityLevel);
                    // 4. 업데이트된 엔티티를 DB에 저장 (JPA는 변경 감지 후 자동으로 UPDATE 쿼리 실행)
                    densityAreaInfoRepository.save(areaInfo);
                    updatedCount++;
                    System.out.println("업데이트 성공: 지역=" + areaNm + ", 밀집도 레벨=" + densityLevel);
                } else {
                    System.err.println("API로부터 밀집도 레벨을 가져오지 못함 (지역: " + areaNm + "). 스킵합니다.");
                }
            } catch (Exception e) {
                System.err.println("API 호출 또는 DB 업데이트 중 오류 발생 (지역: " + areaNm + "): " + e.getMessage());
                e.printStackTrace(); // 예외 스택 트레이스 출력
                // 필요하다면 이곳에서 롤백 로직을 추가하거나, 특정 오류에 대한 처리를 다르게 할 수 있습니다.
            }
        }
        System.out.println("API를 통한 인구 밀집도 레벨 업데이트 완료. 총 " + updatedCount + "개 지역 업데이트.");
    }
}
//구현 후 주석 지우고 @EnableScheduling 활성화 시키기
//    // 10분마다 실행되는 스케줄링 함수
//    @Scheduled(fixedRate = 10 * 60 * 1000) // 10분마다 (ms 단위)
//    public void fetchDensityPeriodically() {
//        fetchAndSaveDensity("광화문덕수궁"); // 원하는 지역명
//        System.out.println("✅ 밀집도 정보 저장 완료");
