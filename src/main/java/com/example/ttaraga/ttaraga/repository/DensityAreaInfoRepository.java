package com.example.ttaraga.ttaraga.repository;

import com.example.ttaraga.ttaraga.entity.DensityAreaInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//인구 밀집도 데이터를 가져 올 때 도시이름이 필요한데 db에 도시 이름 넣을 때 사용

@Repository
public interface DensityAreaInfoRepository extends JpaRepository<DensityAreaInfo,Long> {

    Optional<DensityAreaInfo> findByAreaNm(String areaNm);
}
