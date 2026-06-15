package com.lifelink.util;

import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        // Minimum 6 characters
        return password != null && password.length() >= 6;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        // Allows digits, spaces, hyphens, and optional + prefix
        String cleanPhone = phone.replaceAll("[\\s\\-+]", "");
        return cleanPhone.matches("\\d{7,15}");
    }

    public static boolean isValidAge(int age) {
        return age >= 18 && age <= 65;
    }
}
