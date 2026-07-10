package com.digital5.exception;

import com.digital5.logger.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.digital5.data.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<String> handleErrors(Throwable exception) {

        if (exception instanceof DigitalException digitalException) {
            Logger.logBackendException(digitalException);
            ErrorResponse errorResponse = new ErrorResponse(digitalException.getStatusCode(), digitalException.getMessage());
            return errorResponse.toResponseEntity();

        }else if (exception instanceof HttpRequestMethodNotSupportedException) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Invalid request. Stop trying to hack us!");
            return errorResponse.toResponseEntity();

        }else if (exception instanceof HttpMessageNotReadableException) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request. Stop trying to hack us!");
            return errorResponse.toResponseEntity();

        }else if (exception instanceof MethodArgumentNotValidException) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body: missing required fields.");
            return errorResponse.toResponseEntity();

        }else if (exception instanceof Exception e) {
            Logger.logError(e);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unknown error occurred.");
            return errorResponse.toResponseEntity();

        }else {
            log.error("Fatal error occurred: {}", exception.getMessage(), exception);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unknown error occurred.");
            return errorResponse.toResponseEntity();
        }

    }
}