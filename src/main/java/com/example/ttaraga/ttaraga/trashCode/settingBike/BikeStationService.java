package com.example.ttaraga.ttaraga.trashCode.settingBike;
/*

따릉이 스테이션 정보를 조회하거나 저장하는 비즈니스 로직을 담당합니다.
이 서비스는 @Scheduled가 붙은 데이터 업데이트 로직을 포함하지 않고,
순수하게 CRUD(조회, 저장 등) 작업만 담당합니다.

스케쥴은 다른 서비스 코드에서
 */

import com.example.ttaraga.ttaraga.entity.Bike;
import com.example.ttaraga.ttaraga.repository.BikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) //읽기전용
public class BikeStationService {
    private final BikeRepository bikeRepository;

    //모든 따릉이 정거장 정보 조회
    public List<Bike> getAllStations(){
        return bikeRepository.findAll();
    }

    //특정 따릉이 스테이션 ID로 조회
    public Optional<Bike> getStationById(String stationId){
        return bikeRepository.findByApiStationId(stationId);
    }

    //새 스테이션 저장 혹은 업데이트
    @Transactional
    public Bike saveStation(Bike bike){
        return bikeRepository.save(bike);
    }

    //여러 정거장 저장
    @Transactional
    public List<Bike> saveAllStations(List<Bike> stations){
        return bikeRepository.saveAll(stations);
    }

    //스테이션 개수 조회
    public long countStations(){
        return bikeRepository.count();
    }

}
