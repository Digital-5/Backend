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

    public static String stringToHex(String str) {

        StringBuilder hexString = new StringBuilder();
        char[] characters = str.toCharArray();
        for (char c : characters) {
            int intValue = (int) c; //each char gets transformed into Unicode
            String hexValue = Integer.toHexString(intValue); //and then to hex
            hexString.append(hexValue);
        }
        return hexString.toString();
    }

    public static String hexToString(String hexStr) {
        char[] tempchar = hexStr.toCharArray();
        StringBuilder outputString = new StringBuilder();

        for (int x = 0; x < tempchar.length; x += 2) {
            String tempString = "" + tempchar[x] + tempchar[x + 1]; //Alle 2 zeichen zusammen
            char character = (char) Integer.parseInt(tempString, 16); //die 2 hexzeichen zu einem int umgewandelt und dann zu einem char gecastet
            outputString.append(character);
        }

        return outputString.toString();
    }
}
