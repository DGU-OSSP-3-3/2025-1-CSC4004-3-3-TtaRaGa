package com.example.ttaraga.ttaraga.utility;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class StringToLongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return 0L; // null이거나 빈 문자열인 경우 0으로 처리
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            // 파싱 오류 발생 시 (예: "abc" 같은 잘못된 숫자 문자열)
            // 에러를 던지거나, 기본값으로 처리하거나, 로깅할 수 있습니다.
            // 여기서는 0으로 처리하고 오류를 로깅합니다.
            System.err.println("Error parsing String to Long: " + value + ". Defaulting to 0.");
            return 0L;
            // 또는 throw new IOException("Cannot parse '" + value + "' to Long", e);
        }
    }
}