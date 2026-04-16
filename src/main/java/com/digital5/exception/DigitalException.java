package com.digital5.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class DigitalException extends Exception {

    private final String message;
    @Getter
    private final HttpStatus statusCode;

    public DigitalException(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
