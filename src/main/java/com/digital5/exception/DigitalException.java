package com.digital5.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class DigitalException extends Exception {

    private final String message;
    @Getter
    private final HttpStatus statusCode;

    public DigitalException(HttpStatus statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
