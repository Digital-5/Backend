package com.digital5.service;

public class StringService {

    public static boolean isValidString(String str, int maxLength) {
        if (str == null) {
            return false;
        } // Handle null case
        else {
            String str2 = str.trim(); // Step 1: Trim whitespace
            str2 = str2.replaceAll("[^\\w\\s]", ""); // Step 2: Remove special characters
            str2 = str2.replaceAll("\\s+", ""); // Step 3: Replace multiple spaces with a single space

            return str.length() <= maxLength && !str.isEmpty() && str.equals(str2);
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

    public static String stringToC4(String str) {
        StringBuilder c4String = new StringBuilder();
        char[] characters = str.toCharArray();
        for (char c : characters) {
            int intValue = (int) c; //each char gets transformed into Unicode
            String c4Value = Integer.toString(intValue, 36); //and then to base 36
            // Pad to 2 characters for consistent decoding
            if (c4Value.length() < 2) {
                c4String.append("0");
            }
            c4String.append(c4Value);
        }
        return c4String.toString();
    }

    public static String c4ToString(String c4Str) {
        char[] tempchar = c4Str.toCharArray();
        StringBuilder outputString = new StringBuilder();

        for (int x = 0; x < tempchar.length; x += 2) {
            String tempString = "" + tempchar[x] + tempchar[x + 1]; //Alle 2 zeichen zusammen
            char character = (char) Integer.parseInt(tempString, 36); //die 2 base36-Zeichen zu einem int und dann zu einem char
            outputString.append(character);
        }
        return outputString.toString();
    }
}