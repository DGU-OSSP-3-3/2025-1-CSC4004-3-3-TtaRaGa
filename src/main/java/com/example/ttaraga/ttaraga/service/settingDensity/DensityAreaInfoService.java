package com.example.ttaraga.ttaraga.service.settingDensity;

import com.example.ttaraga.ttaraga.entity.DensityAreaInfo;
import com.example.ttaraga.ttaraga.repository.DensityAreaInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/*
DensityAreaInfo 엔티티와 관련된 데이터베이스 작업을 처리하는 중간 다리 역할을 합니다.

DataInitializer나 다른 서비스에서 직접 DensityAreaInfoRepository를 호출하는 대신,
DensityAreaInfoService를 통해 비즈니스 로직을 캡슐화하고 데이터베이스 접근을 추상화함으로써
코드의 재사용성, 유지보수성, 테스트 용이성을 높여줍니다.

이 서비스는 특히 애플리케이션이 처음 시작될 때 엑셀 데이터를 DB에 일괄 저장하는 데 핵심적인 역할을 합니다.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class DensityAreaInfoService {

    private final DensityAreaInfoRepository areaInfoRepository;

    //엑셀에서 읽고 한꺼번에 저장 매서드
    public void saveAll(List<DensityAreaInfo> areaInfoList){
        areaInfoRepository.saveAll(areaInfoList);
    }

    //전체 조회
    public List<DensityAreaInfo> findAll(){
        return areaInfoRepository.findAll();
    }
    //현 db 열 반환
    public long count() {
        return areaInfoRepository.count();
    }
}
