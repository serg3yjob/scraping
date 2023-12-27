package ru.scraping.data.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;

@ControllerAdvice
public class Advisor {

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleBadRequestException(Exception exception) {
        return ResponseEntity.internalServerError()
                             .body(Map.of("description", exception.getMessage(),
                                          "code", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }
}
