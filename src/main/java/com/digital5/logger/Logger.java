package com.digital5.logger;

import com.digital5.exception.DigitalException;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class Logger {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger("Digital5");

    public static void log(String logging) {
        log.info(logging);
    }

    public static void log(LogLevel loglevel, String logging) {
        switch (loglevel) {
            case DEBUG -> log.debug(logging);
            case INFO -> log.info(logging);
            case WARN -> log.warn(logging);
            case ERROR -> log.error(logging);
        }
    }

    public static void logError(Exception error) {
        log.error("Error Occurred! Message: {}", error.getMessage(), error);
    }

    public static void logBackendException(DigitalException error) {
        HttpStatus code = error.getStatusCode();
        String message = error.getMessage();
        if (code == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Digital Exception Occurred! Message: {}; Status Code: {}", message, code);
        } else {
            log.warn("Digital Exception Occurred! Message: {}; Status Code: {}", message, code);
        }
    }

}