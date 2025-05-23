package com.example.ttaraga.ttaraga.service.settingAttractingPlaces;

import com.example.ttaraga.ttaraga.dto.AttractingPlaceDto;
import com.example.ttaraga.ttaraga.entity.AttractingPlace;
import com.example.ttaraga.ttaraga.mapper.DtoMapper;
import com.example.ttaraga.ttaraga.repository.AttractingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입
public class AttractingPlaceService {

    private final AttractingRepository repository;
    private final DtoMapper dtoMapper;
    // ExcelReader는 이 서비스 내에서 직접 인스턴스화하여 사용합니다. (또는 스프링 빈으로 등록하고 주입받을 수 있음)
    // 현재는 유틸리티 클래스이므로 직접 인스턴스화해도 무방합니다.

    /**
     * 엑셀 파일로부터 명소 데이터를 읽어와 데이터베이스에 저장.
     * ExcelReader를 사용하여 DTO 리스트를 먼저 얻은 후, 이를 엔티티로 변환하여 저장.
     * @param file 엑셀 MultipartFile
     * @return 저장된 AttractingPlace 엔티티 리스트
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public List<AttractingPlace> savePlacesFromExcel(MultipartFile file) throws IOException {
        // ExcelReader 인스턴스 생성 (필요에 따라 스프링 빈으로 만들고 주입받을 수도 있습니다)
        ExcelReader excelReader = new ExcelReader();
        List<AttractingPlaceDto> dtos = excelReader.readPlacesFromExcel(file);

        // DTO 리스트를 Entity 리스트로 변환
        List<AttractingPlace> places = dtos.stream()
                .map(dtoMapper::toAttractingPlaceEntity)
                .collect(Collectors.toList());

        // 변환된 엔티티 리스트를 데이터베이스에 저장
        return repository.saveAll(places);
    }

    // 명소 조회 등 다른 비즈니스 로직 추가
    public List<AttractingPlaceDto> getAllAttractingPlaces() {
        return repository.findAll().stream()
                .map(dtoMapper::toAttractingPlaceDto)
                .collect(Collectors.toList());
    }

    public AttractingPlaceDto getAttractingPlaceById(Long id) {
        return repository.findById(String.valueOf(id))
                .map(dtoMapper::toAttractingPlaceDto)
                .orElse(null);
    }
}