package com.example.ttaraga.ttaraga.service.settingAccInfo;

import com.example.ttaraga.ttaraga.api.AccInfoAPIClient;
import com.example.ttaraga.ttaraga.dto.AccInfoDto;
import com.example.ttaraga.ttaraga.entity.AccInfo;
import com.example.ttaraga.ttaraga.mapper.DtoMapper;
import com.example.ttaraga.ttaraga.repository.AccInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccInfoService {
    private final AccInfoRepository accInfoRepository;
    private final DtoMapper dtoMapper;
    private final AccInfoAPIClient accInfoAPIClient;
    private final ConvertTMtoLanLon convertTMtoLanLon;
    private ConvertTMtoLanLon TMtoLanLon;

    @Scheduled(fixedRate = 3600000)  // 1시간(3600초)마다 실행
    @Transactional
    public void fetchAndSaveAccInfo() {
        accInfoAPIClient.fetchAccInfo(1, 10)
                .flatMap(this::parseAndSaveAccInfo)
                .onErrorResume(e -> {
                    System.err.println("AccInfo 처리 중 에러: " + e.getMessage());
                    return Mono.empty();
                })
                .block();
    }

    @SneakyThrows
    private Mono<Void> parseAndSaveAccInfo(String xmlResponse) {
        try {
            // XML 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));
            NodeList rowList = doc.getElementsByTagName("row");

            List<AccInfoDto> accInfoDtos = new ArrayList<>();
            for (int i = 0; i < rowList.getLength(); i++) {
                Element row = (Element) rowList.item(i);
                AccInfoDto dto = new AccInfoDto();

                // XML 필드 추출
                dto.setAcc_id(getElementValue(row, "acc_id"));
                dto.setOccr_time(getElementValue(row, "occr_time"));
                dto.setAcc_info(getElementValue(row, "acc_info"));

                // TMX/TMY를 위도/경도로 변환
                String tmxStr = getElementValue(row, "grs80tm_x");
                String tmyStr = getElementValue(row, "grs80tm_y");
                if (tmxStr != null && tmyStr != null) {
                    try {
                        double tmx = Double.parseDouble(tmxStr);
                        double tmy = Double.parseDouble(tmyStr);
                        double[] latLon = convertTMtoLanLon.TMtoLanLon(tmx, tmy);
                        dto.setAcc_latitude(latLon[0]);
                        dto.setAcc_longitude(latLon[1]);
                    } catch (NumberFormatException e) {
                        System.err.println("TM 좌표 변환 실패: " + e.getMessage());
                        continue; // 좌표 변환 실패 시 해당 데이터 건너뜀
                    }
                } else {
                    continue; // 좌표 없으면 건너뜀
                }

                accInfoDtos.add(dto);
            }

            // DB 저장
            List<AccInfo> accInfos = accInfoDtos.stream()
                    .map(dtoMapper::toAccInfoEntitiy)
                    .collect(Collectors.toList());
            accInfoRepository.deleteAll();  // 기존 데이터 삭제(필요 시 수정)
            accInfoRepository.saveAll(accInfos);

            return Mono.empty();
        } catch(Exception e){
            return Mono.error(new RuntimeException("XML 파싱 또는 DB 저장 실패: "+e.getMessage()));
        }
    }

    private String getElementValue(Element row, String tagName) {
        NodeList nodeList = row.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
}
