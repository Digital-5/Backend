package com.digital5.service;

public class StringService {

    public static String cleanString(String str) {
        if (str == null) return ""; // Handle null case
        str = str.trim(); // Step 1: Trim whitespace
        str = str.replaceAll("\\W+", ""); // Step 2: Remove special characters
        str = str.replaceAll("\s+", " "); // Step 3: Replace multiple spaces with a single space
        return str;
    }

    public static String sizeLimitString(String str, int maxLength) {
        if (str == null) return ""; // Handle null case
        if (str.length() <= maxLength) {
            return str;
        } else {
            return str.substring(0, maxLength); // schneidet alles nach max length ab
        }
    }
}
