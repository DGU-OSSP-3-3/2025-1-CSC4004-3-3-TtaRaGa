package com.example.ttaraga.ttaraga.api;


import com.example.ttaraga.ttaraga.dto.CityDataResponse_NEW;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/*
API 코드 호출하는거 새로 작성했습니다.

호출하는거 시간 재봤는데 120개 정도 데이터의 api불러오는데 대략 2분 20초 걸림

api키값은 application.properties에 몰아놨습니다. for 보안 유지

서울 밀집도 api 내용 불러오는 코드임 -> 서비스 코드에서 사용
 */

@Component
public class DensityNewAPIClient {

    @Value("${seoul.api.key}")
    private String serviceKey;

    private static final String BASE_URL = "http://openapi.seoul.go.kr:8088";
    private final ObjectMapper objectMapper; // objectMapper 주입

    // 생성자에서 ObjectMapper를 주입받도록 변경합니다.
    // Spring Boot는 RestTemplate처럼 ObjectMapper도 자동으로 빈으로 등록해줍니다.
    public DensityNewAPIClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * HttpURLConnection을 사용하여 특정 지역의 인구 밀집도 레벨을 서울시 API에서 가져와 반환합니다.
     * @param areaName API로 조회할 지역 이름 (area_nm)
     * @return 해당 지역의 인구 밀집도 레벨 문자열 (예: "여유", "보통"), API 호출 실패 또는 데이터 없을 시 null 반환
     */

    public String getPopulationDensityLevel(String areaName) {
        HttpURLConnection conn = null;
        BufferedReader rd = null;
        StringBuilder sb = new StringBuilder();

        try {
            String encodedAreaName = URLEncoder.encode(areaName, StandardCharsets.UTF_8.toString()); // UTF-8 지정
            String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());

            // URL 빌더를 사용하여 URL 생성 (RestTemplate 방식과 유사하게)
            URI uri = new URI(
                    BASE_URL + "/" + encodedServiceKey +
                            "/json/citydata/1/1/" + encodedAreaName // 시작/종료 인덱스 1/1로 변경하여 한 개의 결과만 요청
            );

            URL url = uri.toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json"); // JSON 응답 요청
            conn.setConnectTimeout(5000); // 5초
            conn.setReadTimeout(5000);   // 5초

            System.out.println("API 호출 URL: " + url.toString());
            int responseCode = conn.getResponseCode();
            System.out.println("API 응답 코드: " + responseCode);

            // 응답 코드에 따라 InputStream 또는 ErrorStream 가져오기
            if (responseCode >= 200 && responseCode <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            // 응답 내용을 모두 읽기
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            String jsonResponse = sb.toString();
            System.out.println("Full JSON Response:\n" + jsonResponse);

            // ✅ Jackson ObjectMapper를 사용하여 JSON을 CityDataResponse DTO로 파싱
            CityDataResponse_NEW response = objectMapper.readValue(jsonResponse, CityDataResponse_NEW.class);

            // DTO에서 필요한 'AREA_CONGEST_LVL' 추출
            if (response != null &&
                    response.getCityData() != null && // CITYDATA 객체에 접근
                    response.getCityData().getLivePopulationStatus() != null && // LIVE_PPLTN_STTS 리스트에 접근
                    !response.getCityData().getLivePopulationStatus().isEmpty()) { // 리스트가 비어있지 않은지 확인

                // 첫 번째 LivePopulationStatus 항목에서 AREA_CONGEST_LVL 추출
                return response.getCityData().getLivePopulationStatus().get(0).getAreaCongestLvl();
            } else {
                System.err.println("API 응답에서 유효한 데이터 또는 밀집도 레벨을 찾을 수 없습니다: " + areaName);
                // API 응답이 왔지만, 데이터가 없거나 구조가 맞지 않는 경우
                return null;
            }

        } catch (IOException e) {
            System.err.println("API 호출 중 IO 오류 (지역명: " + areaName + "): " + e.getMessage());
            return null; // 네트워크 또는 데이터 읽기 오류 시 null 반환
        } catch (Exception e) { // URI, JSON 파싱 등 기타 예외
            System.err.println("알 수 없는 API 호출/파싱 오류 (지역명: " + areaName + "): " + e.getMessage());
            e.printStackTrace(); // 자세한 스택 트레이스 출력
            return null;
        } finally {
            try {
                if (rd != null) rd.close();
            } catch (IOException e) {
                System.err.println("BufferedReader 닫기 오류: " + e.getMessage());
            }
            if (conn != null) conn.disconnect();
        }
    }



}
