package com.example.ttaraga.ttaraga.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)  // 404 상태 반환
public class NoValidRouteFoundException extends RuntimeException {
    public NoValidRouteFoundException(String message) {
        super(message);
    }
}