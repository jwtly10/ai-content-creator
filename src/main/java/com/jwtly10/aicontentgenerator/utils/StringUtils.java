package com.jwtly10.aicontentgenerator.utils;

public class StringUtils {

    public static boolean isCapitalized(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return Character.isUpperCase(s.charAt(0));
    }

    public static boolean endsWithPunctuation(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return s.endsWith(".") || s.endsWith(",") || s.endsWith("!") || s.endsWith("?") || s.endsWith(":") || s.endsWith(";");
    }
}
