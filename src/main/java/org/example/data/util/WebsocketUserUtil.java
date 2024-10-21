package org.example.data.util;

import java.util.regex.Pattern;

public class WebsocketUserUtil {
    private static final Pattern UsernamePattern = Pattern.compile("[a-zA-Zā-žĀ-Ž0-9_]{3,20}+");
    private WebsocketUserUtil() {}

    // Method to validate input string against the regex pattern
    public static boolean ValidUsername(String input) {
        if (input == null) {
            return false; // Optionally handle null inputs
        }
        return UsernamePattern.matcher(input).matches();
    }
}
