package com.merrycodes.utils;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author MerryCodes
 * @date 2020/6/15 9:43
 */
public class EncryptUtils {

    @SneakyThrows
    public static String MD5(String value) {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] bytes = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
        return toHex(bytes);
    }

    @SneakyThrows
    public static String MD5(byte[] valueBytes) {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] bytes = messageDigest.digest(valueBytes);
        return toHex(bytes);
    }

    private static String toHex(byte[] bytes) {
        final char[] HEX_DIGITS = "merrycodesA1B4C9".toCharArray();
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(HEX_DIGITS[(value >> 4) & 0x0f]);
            builder.append(HEX_DIGITS[value & 0x0f]);
        }
        return builder.toString();
    }

}
