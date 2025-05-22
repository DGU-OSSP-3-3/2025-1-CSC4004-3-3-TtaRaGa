package com.example.ttaraga.ttaraga.trashCode;

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
import java.util.Optional;

@Component
public class BikeNewAPIClient {

    @Value("${seoul.api.key}")
    private String apiKey;

    private static final String BASE_URL = "http://openapi.seoul.go.kr:8088";
    private static final String SERVICE_NAME = "bikeList"; // 따릉이 서비스 이름
    private final ObjectMapper objectMapper; // ObjectMapper 주입

    // 생성자를 통해 ObjectMapper를 주입받습니다.
    public BikeNewAPIClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 서울시 따릉이 API를 호출하여 특정 범위의 대여소 정보를 가져옵니다.
     * HttpURLConnection을 사용하여 API를 호출하고 JSON 응답을 파싱합니다.
     *
     * @param startIdx 요청 시작 인덱스
     * @param endIdx   요청 종료 인덱스
     * @return BikeAPIResponse DTO 객체 (API 호출 실패 시 Optional.empty() 반환)
     */
    public Optional<BikeAPIResponse> fetchBikeStations(int startIdx, int endIdx) {
        HttpURLConnection conn = null;
        BufferedReader rd = null;
        StringBuilder sb = new StringBuilder();

        try {
            // API 키와 서비스 이름을 URL 인코딩합니다.
            String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8.toString());
            String encodedServiceName = URLEncoder.encode(SERVICE_NAME, StandardCharsets.UTF_8.toString());
            String responseType = URLEncoder.encode("json", StandardCharsets.UTF_8.toString()); // 응답 파일 타입

            // URL을 StringBuilder와 String.format을 조합하여 구성합니다.
            // DensityNewAPIClient의 URI 구성 방식을 따릅니다.
            URI uri = new URI(
                    String.format("%s/%s/%s/%s/%d/%d",
                            BASE_URL,
                            encodedApiKey,
                            responseType,
                            encodedServiceName,
                            startIdx,
                            endIdx
                    )
            );

            URL url = uri.toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json"); // JSON 응답을 요청합니다.
            conn.setConnectTimeout(5000); // 연결 타임아웃 5초
            conn.setReadTimeout(5000);   // 읽기 타임아웃 5초

            System.out.println("따릉이 API 호출 URL: " + url.toString());
            int responseCode = conn.getResponseCode();
            System.out.println("따릉이 API 응답 코드: " + responseCode);

            // 응답 코드가 200~300 사이면 정상 스트림, 아니면 에러 스트림을 가져옵니다.
            if (responseCode >= 200 && responseCode <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            // 응답 내용을 모두 읽어 StringBuilder에 저장합니다.
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            String jsonResponse = sb.toString();
            // 디버깅 목적으로 전체 JSON 응답을 출력할 수 있으나, 너무 길면 성능에 영향
            // System.out.println("따릉이 API Full JSON Response (Idx " + startIdx + "-" + endIdx + "):\n" + jsonResponse.substring(0, Math.min(jsonResponse.length(), 500)) + "...");

            // Jackson ObjectMapper를 사용하여 JSON을 BikeAPIResponse DTO로 파싱합니다.
            BikeAPIResponse apiResponse = objectMapper.readValue(jsonResponse, BikeAPIResponse.class);

            // 파싱된 DTO에서 API 호출 결과를 검증하고 Optional<BikeAPIResponse>를 반환합니다.
            if (apiResponse != null &&
                    apiResponse.getBikeStatus() != null && // JSON 응답의 "rentBikeStatus" 필드에 접근
                    "INFO-000".equals(apiResponse.getBikeStatus().getResult().getCode())) { // 그 안의 "RESULT" 객체와 "CODE" 필드에 접근
                return Optional.of(apiResponse);
            } else {
                // API 응답이 실패했거나 예상 구조와 다른 경우 에러 메시지를 출력하고 Optional.empty() 반환
                String errorMessage = (apiResponse != null && apiResponse.getBikeStatus() != null && apiResponse.getBikeStatus().getResult() != null)
                        ? apiResponse.getBikeStatus().getResult().getMessage()
                        : "알 수 없는 응답 오류 (API 응답 구조 불일치 또는 데이터 없음)";
                System.err.println("따릉이 API 응답 오류: " + errorMessage + " (인덱스: " + startIdx + "-" + endIdx + ")");
                return Optional.empty();
            }

        } catch (IOException e) {
            // 네트워크 연결, 데이터 읽기 등 IO 관련 오류 처리
            System.err.println("따릉이 API 호출 중 IO 오류 (인덱스: " + startIdx + "-" + endIdx + "): " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) { // URISyntaxException, JsonProcessingException 등 기타 예외 처리
            System.err.println("따릉이 API 호출/파싱 중 알 수 없는 오류 (인덱스: " + startIdx + "-" + endIdx + "): " + e.getMessage());
            e.printStackTrace(); // 개발 중에는 자세한 스택 트레이스를 출력하는 것이 좋습니다.
            return Optional.empty();
        } finally {
            // 사용한 자원(BufferedReader, HttpURLConnection)을 확실히 해제합니다.
            try {
                if (rd != null) rd.close();
            } catch (IOException e) {
                System.err.println("BufferedReader 닫기 오류: " + e.getMessage());
            }
            if (conn != null) conn.disconnect();
        }
        //return Optional.empty();
    }

    /**
     * API에서 총 따릉이 스테이션 개수를 가져오는 메서드.
     * 이 메서드는 API에 1개만 요청하여 전체 데이터 개수를 파악합니다.
     *
     * @return 총 따릉이 스테이션 개수, 실패 시 0 반환
     */
    public int getTotalStationCount() {
        // 첫 번째 페이지의 데이터만 요청하여 list_total_count를 가져옵니다.
        Optional<BikeAPIResponse> responseOpt = fetchBikeStations(1, 1);
        return responseOpt.map(BikeAPIResponse::getBikeStatus) // "rentBikeStatus" 객체에 접근
                .map(BikeAPIResponse.RentBikeStatus::getListTotalCount) // "list_total_count" 필드에 접근
                .orElse(0); // Optional이 비어있거나 필드를 찾을 수 없으면 0 반환
    }
}



