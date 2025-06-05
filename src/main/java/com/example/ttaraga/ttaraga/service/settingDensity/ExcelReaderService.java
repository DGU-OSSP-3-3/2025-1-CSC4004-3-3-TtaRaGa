package com.example.ttaraga.ttaraga.service.settingDensity;

import com.example.ttaraga.ttaraga.entity.DensityAreaInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.example.ttaraga.ttaraga.service.settingDensity.GeoJsonGernerator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

//
/*
밀집도 관련 엑셀 데이터 읽어오는 코드
DensityAreaInfo.xmlx 전용으로 작성했음
열에 따라 다음과 같은 내용을 가짐
예시: 0: Category, 1: No, 2: AreaCd, 3: AreaNm, 4: EngNm, 5: 위도, 6: 경도
해당 내용 읽는 코드임

다른 엑셀 파일 읽고 싶으면 이 코드 바탕으로 재탕 gg
 */

@Service
public class ExcelReaderService {


    private final GeoJsonGernerator geoJsonGernerator;

    public ExcelReaderService(GeoJsonGernerator geoJsonGenerator) {
        this.geoJsonGernerator = geoJsonGenerator;
    }


    public List<DensityAreaInfo> readAreaInfoFromExcel(InputStream inputStream) throws Exception{
//        System.out.println("엑셀 읽는중 .,,,.");
        List<DensityAreaInfo> areaInfoList = new ArrayList<>();

        Workbook workbook = new XSSFWorkbook(inputStream);
        //Sheet sheet = workbook.getSheetAt(0);
        Sheet sheet = workbook.getSheet("장소목록");

        int rowIndex =0;
        for (Row row : sheet) {
            // 첫 행은 헤더이므로 건너뛰기
            if (rowIndex == 0) {
                //System.out.println("첫 행(헤더) 건너뛰기");
                rowIndex++;
                continue;
            }

            // ✅ 현재 행이 완전히 비어있는지 확인하여 건너뛰기
            if (isRowEmpty(row)) {
                //System.out.println("비어있는 행 건너뛰기: " + rowIndex);
                rowIndex++; // 비어있는 행도 인덱스는 증가시켜야 정확한 행 번호를 유지합니다.
                continue;
            }

//            System.out.println("데이터 행 읽기: " + rowIndex);

            DensityAreaInfo areaInfo = new DensityAreaInfo();
            areaInfo.setCategory(getCellStringValue(row.getCell(0)));
            areaInfo.setNo(getCellStringValue(row.getCell(1))); // 엑셀 컬럼 인덱스 확인
            areaInfo.setAreaCd(getCellStringValue(row.getCell(2)));
            areaInfo.setAreaNm(getCellStringValue(row.getCell(3)));
            areaInfo.setEngNm(getCellStringValue(row.getCell(4)));
            areaInfo.setLatitude(getCellNumericValue(row.getCell(5),"위도"));
            areaInfo.setLongitude(getCellNumericValue(row.getCell(6),"경도"));

            Double latitude = getCellNumericValue(row.getCell(5), "위도");
            Double longitude = getCellNumericValue(row.getCell(6), "경도");

            if (latitude != null && longitude != null) { // areaNm.isEmpty() 체크는 GeoJSON 생성과 분리
                areaInfo.setLatitude(latitude);
                areaInfo.setLongitude(longitude);

                // ⚡️ GeoJSON 생성: 위도와 경도만 전달
                String geoJsonData = geoJsonGernerator.generateSquareGeoJson(latitude, longitude);

                if (geoJsonData != null) {
                    areaInfo.setGeoJson(geoJsonData);
                } else {
                    System.err.println("경고: 위도 " + latitude + ", 경도 " + longitude + "에 대한 GeoJSON 생성 실패.");
                }

                // ⚡️ 우선순위 설정 (초기값 또는 다른 로직으로)
                // 이 시점에는 실제 밀집도 레벨을 모르므로, 초기값을 설정하거나 비워둡니다.
                // 보통 GraphHopper에서 가중치는 밀집도 레벨에 따라 동적으로 부여되므로,
                // 엑셀에서 로드하는 이 시점에는 우선순위를 0으로 설정하거나, 아예 설정하지 않고
                // 나중에 밀집도 API 호출 시에 업데이트하는 것이 일반적입니다.
                // 여기서는 "정보 없음" 레벨에 해당하는 우선순위를 기본으로 설정합니다.


            } else {
                System.err.println("경고: 행 " + (rowIndex + 1) + "의 위도/경도 데이터가 유효하지 않아 GeoJSON 생성을 건너뜁니다.");
            }
            areaInfoList.add(areaInfo);
            rowIndex++;
        }


        workbook.close();
        return areaInfoList;
    }

    private String getCellStringValue(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }
    // ⚡️ 셀 값을 Double (숫자)로 안전하게 가져오는 헬퍼 메서드
    private Double getCellNumericValue(Cell cell, String fieldName) {
        if (cell == null) {
            System.err.println("경고: '" + fieldName + "' 셀이 비어있습니다.");
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                // 문자열 형태의 숫자를 Double로 파싱 시도
                String stringValue = cell.getStringCellValue().trim();
                if (stringValue.isEmpty()) {
                    System.err.println("경고: '" + fieldName + "' 셀이 비어있습니다 (문자열).");
                    return null;
                }
                return Double.parseDouble(stringValue);
            } else if (cell.getCellType() == CellType.BLANK) {
                System.err.println("경고: '" + fieldName + "' 셀이 비어있습니다 (BLANK).");
                return null;
            } else {
                System.err.println("경고: '" + fieldName + "' 셀 타입이 숫자가 아닙니다: " + cell.getCellType() + ", 값: '" + getCellStringValue(cell) + "'");
                return null;
            }
        } catch (NumberFormatException e) {
            System.err.println("오류: '" + fieldName + "' 셀 값 '" + getCellStringValue(cell) + "'을(를) 숫자로 변환할 수 없습니다: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("오류: '" + fieldName + "' 셀 처리 중 예기치 않은 오류 발생: " + e.getMessage());
            return null;
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        // getPhysicalNumberOfCells()는 실제로 데이터가 있는 셀만 반환
        // getFirstCellNum()과 getLastCellNum()을 이용하여 전체 셀을 확인하는 것이 더 정확
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            // 셀이 null이 아니고, 빈 타입이 아니며, 문자열로 변환했을 때 비어있지 않으면 데이터가 있는 것으로 간주
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellStringValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}