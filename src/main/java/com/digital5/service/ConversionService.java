package com.digital5.service;

import org.springframework.stereotype.Service;

@Service
public class ConversionService {

    public String stringToHex(String str) {

        StringBuilder hexString = new StringBuilder();
        char[] characters = str.toCharArray();
        for (char c : characters) {
            int intValue = c; //each char gets transformed into Unicode
            String hexValue = Integer.toHexString(intValue); //and then to hex
            hexString.append(hexValue);
        }
        return hexString.toString();
    }

    /**
     * concatenates the given arrays into one
     *
     * @param arrays the arrays to concatenate
     * @return the concatenated array
     * */
    public byte[] concatenateByteArrays(byte[][] arrays) {

        //calculate the number of bytes needed for the result array
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : arrays) {
            for (int j = 0; j < array.length; j++) {
                result[j + offset] = array[j];
            }
            offset += array.length;
        }
        return result;
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
