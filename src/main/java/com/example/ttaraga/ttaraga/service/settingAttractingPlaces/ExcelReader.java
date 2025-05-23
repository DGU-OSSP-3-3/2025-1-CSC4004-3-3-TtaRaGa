package com.example.ttaraga.ttaraga.service.settingAttractingPlaces;

import com.example.ttaraga.ttaraga.dto.AttractingPlaceDto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    // 순수하게 엑셀 파일을 읽고 DTO 리스트를 반환하는 유틸리티 역할만 수행.

    public List<AttractingPlaceDto> readPlacesFromExcel(MultipartFile file) throws IOException {
        List<AttractingPlaceDto> dtos = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트 사용
            boolean skipHeader = true; // 첫 번째 행(헤더) 스킵

            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                // 빈 행은 건너뛰기
                if (isRowEmpty(row)) {
                    continue;
                }

                AttractingPlaceDto dto = new AttractingPlaceDto();

                // 셀 값 읽기 (인덱스 주의: 0부터 시작)
                dto.setPlaceId(getStringCellValue(row.getCell(0), dataFormatter));
                dto.setPlaceName(getStringCellValue(row.getCell(1), dataFormatter));
                dto.setPlaceLongitude(getNumericCellValue(row.getCell(2), dataFormatter));
                dto.setPlaceLatitude(getNumericCellValue(row.getCell(3), dataFormatter));
                dto.setDetail(getStringCellValue(row.getCell(4), dataFormatter));

                dtos.add(dto);
            }
            return dtos;
        }
    }

    // --- 엑셀 셀 값 추출 헬퍼 메서드 (public static으로 변경하여 외부에서 쉽게 접근 가능) ---

    private static String getStringCellValue(Cell cell, DataFormatter dataFormatter) {
        if (cell == null) return "";
        return dataFormatter.formatCellValue(cell).trim();
    }

    private static double getNumericCellValue(Cell cell, DataFormatter dataFormatter) {
        if (cell == null) return 0.0;
        try {
            String cellValue = dataFormatter.formatCellValue(cell).trim();
            // 숫자가 텍스트로 저장된 경우를 대비하여 Double.parseDouble 사용
            return Double.parseDouble(cellValue);
        } catch (NumberFormatException e) {
            System.err.println("경고: 숫자 형식이 아닌 값이 발견되었습니다. 셀 값: '" + dataFormatter.formatCellValue(cell) + "' -> 0.0으로 처리");
            return 0.0; // 파싱 실패 시 0.0 반환
        }
    }

    // 빈 행을 체크하는 헬퍼 메서드
    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        // 모든 셀이 null이거나 비어있으면 빈 행으로 간주
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getStringCellValue(cell, new DataFormatter()).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}