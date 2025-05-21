package com.example.ttaraga.ttaraga.service.settingDensity;

import com.example.ttaraga.ttaraga.entity.DensityAreaInfo;
import com.example.ttaraga.ttaraga.repository.DensityAreaInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
