package com.digital5.logger;

import com.digital5.exception.DigitalException;
import org.springframework.http.HttpStatus;

import java.text.SimpleDateFormat;

public class Logger {

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");


    public static void log(String logging) {
        System.out.println(LoggingFormatter.format(logging, LogLevel.INFO));
    }

    public static void log(String logging, LogLevel loglevel)  {
        System.out.println(LoggingFormatter.format(logging, loglevel));
    }

    public static void logError(Exception error) {
        error.printStackTrace();
        Logger.log("Error Occurred! Message: " + error.getMessage() + "; Stack Trace:", LogLevel.ERROR);
        error.printStackTrace();
    }

    public static void logBackendException(DigitalException error) {
        HttpStatus code = error.getStatusCode();
        String message = error.getMessage();
        LogLevel level = code == HttpStatus.INTERNAL_SERVER_ERROR ? LogLevel.ERROR : LogLevel.WARN;
        Logger.log("Digital Exception Occurred! Message: " + message + "; Status Code: " + code, level);
    }

}