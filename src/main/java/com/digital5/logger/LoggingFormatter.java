package com.digital5.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggingFormatter {

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss");

    public static String format(String s, LogLevel loglevel) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("Digital5").append("/");
        sb.append(loglevel.toString());
        sb.append("] (");
        sb.append(dateFormatter.format(new Date()));
        sb.append("): ");
        sb.append(s);
        return sb.toString();
    }

}
