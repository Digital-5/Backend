package com.digital5.exception;

import com.digital5.logger.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import com.digital5.data.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {

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

        }else {
            Logger.logError((Exception) exception);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unknown error occurred.");
            return errorResponse.toResponseEntity();
        }

    }
}