package com.digital5.service;

import org.springframework.stereotype.Service;

@Service
public class ConversionService {

    public String stringToHex(String str) {

        StringBuilder hexString = new StringBuilder();
        char[] characters = str.toCharArray();
        for (char c : characters) {
            int intValue = (int) c; //each char gets transformed into Unicode
            String hexValue = Integer.toHexString(intValue); //and then to hex
            hexString.append(hexValue);
        }
        return hexString.toString();
    }

    public String hexToString(String hexStr) {
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
