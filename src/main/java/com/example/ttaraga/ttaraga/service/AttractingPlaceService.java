package com.example.ttaraga.ttaraga.service;

import com.example.ttaraga.ttaraga.dto.AttractingPlaceDto;
import com.example.ttaraga.ttaraga.entity.AttractingPlace;
import com.example.ttaraga.ttaraga.mapper.DtoMapper;
import com.example.ttaraga.ttaraga.repository.AttractingRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttractingPlaceService {

    private final AttractingRepository repository;
    private final DtoMapper dtoMapper;

    public AttractingPlaceService(AttractingRepository repository, DtoMapper dtoMapper) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
    }

    public List<AttractingPlace> savePlacesFromExcel(MultipartFile file) throws IOException {
        List<AttractingPlace> places = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;

            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                AttractingPlaceDto dto = new AttractingPlaceDto();

                // 셀 값 읽기
                dto.setPlaceId(getStringCellValue(row.getCell(0), dataFormatter));
                dto.setPlaceName(getStringCellValue(row.getCell(1), dataFormatter));
                dto.setPlaceLatitude(getNumericCellValue(row.getCell(2), dataFormatter));
                dto.setPlaceLongitude(getNumericCellValue(row.getCell(3), dataFormatter));
                dto.setDetail(getStringCellValue(row.getCell(4), dataFormatter));

                AttractingPlace place = dtoMapper.toAttractingPlaceEntity(dto);
                places.add(place);
            }

            return repository.saveAll(places);
        }
    }

    private String getStringCellValue(Cell cell, DataFormatter dataFormatter) {
        if (cell == null) return "";
        return dataFormatter.formatCellValue(cell).trim();
    }

    private double getNumericCellValue(Cell cell, DataFormatter dataFormatter) {
        if (cell == null) return 0.0;
        try {
            String cellValue = dataFormatter.formatCellValue(cell).trim();
            return Double.parseDouble(cellValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}