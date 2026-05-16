package com.cic.mobapp.util;

import android.util.Patterns;

public final class Validator {

    private Validator() {}

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    /** Returns 0-3 strength score: 0=too short, 1=weak, 2=medium, 3=strong. */
    public static int passwordStrength(String password) {
        if (password == null || password.length() < 8) return 0;
        int score = 1;
        if (password.chars().anyMatch(Character::isUpperCase) &&
            password.chars().anyMatch(Character::isDigit)) score = 2;
        if (score == 2 && password.chars().anyMatch(c -> "!@#$%^&*_-+=".indexOf(c) >= 0)) score = 3;
        return score;
    }

    public static String passwordStrengthLabel(int score) {
        switch (score) {
            case 0: return "Too short — min 8 characters";
            case 1: return "Weak — add numbers or symbols";
            case 2: return "Medium — add a special character";
            case 3: return "Strong";
            default: return "";
        }
    }
}
