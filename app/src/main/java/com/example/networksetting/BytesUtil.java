package com.example.networksetting;

import java.util.Arrays;

public class BytesUtil {
    private static final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String getHexString(byte b) {
        char[] hexChars = new char[2];
        int number = b & 0xFF;
        hexChars[0] = hexArray[number >>> 4];
        hexChars[1] = hexArray[number & 0x0F];
        return new String(hexChars);
    }

    public static String getHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int number = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[number >>> 4];
            hexChars[j * 2 + 1] = hexArray[number & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getHexString(int number) {
        return Integer.toHexString(number);
    }

    public static byte[] getIntegerToBytes(int number){
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((number >>> 8) & 0x00FF);
        bytes[1] = (byte) (number & 0x00FF);
        return bytes;
    }

    public static byte[] getIntegerToBytes2(int number){
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (number & 0x00FF);
        bytes[1] = (byte) ((number >>> 8) & 0x00FF);
        return bytes;
    }

    public static byte getStringToByte(String hexString) {
        return (byte) Integer.parseInt(hexString, 16);
    }

    public static String getHexString(byte[] array, int start, int end) {
        if (start < 0 || start >= array.length || end < 0 || end >= array.length) {
            return "out of array bounds.";
        } else if (start == 0 && array.length - 1 == end) {
            return getHexString(array);
        } else {
            byte[] bytes = Arrays.copyOfRange(array, start, end + 1);
            return getHexString(bytes);
        }
    }
}
