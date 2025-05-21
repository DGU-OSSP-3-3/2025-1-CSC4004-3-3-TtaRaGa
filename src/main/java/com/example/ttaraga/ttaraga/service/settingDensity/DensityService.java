package com.example.ttaraga.ttaraga.service.settingDensity;


import com.example.ttaraga.ttaraga.api.DensityAPIClient;
import com.example.ttaraga.ttaraga.dto.CityDataResponse;
import com.example.ttaraga.ttaraga.repository.DensityRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DensityService {

    private final DensityAPIClient densityAPIClient;
    private final DensityRepository densityRepository;

    public DensityService(DensityAPIClient densityAPIClient, DensityRepository densityRepository) {
        this.densityAPIClient = densityAPIClient;
        this.densityRepository = densityRepository;
    }

    @Transactional
    public void fetchAndSaveDensity(String areaName) {
        CityDataResponse response = densityAPIClient.getCityData(areaName);
        //List<Densitydto> densityList = 0;
//        for(Densitydto dto : densityList){
//            densityRepository.save(dto.toEntity());
//        }
//    }
    //구현 후 주석 지우고 @EnableScheduling 활성화 시키기
//    // 10분마다 실행되는 스케줄링 함수
//    @Scheduled(fixedRate = 10 * 60 * 1000) // 10분마다 (ms 단위)
//    public void fetchDensityPeriodically() {
//        fetchAndSaveDensity("광화문덕수궁"); // 원하는 지역명
//        System.out.println("✅ 밀집도 정보 저장 완료");
    }

}
