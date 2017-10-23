package com.juanmsl.googlebase.logic;

public class FieldValidator {
    public static boolean validateEmail(String email) {
        return email.matches("[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+.[a-zA-Z0-9_-]+");
    }

    public static boolean validatePassword(String password) {
        return password.matches("[a-zA-Z0-9_-]{6,20}");
    }

    public static boolean validateText(String text) {
        return text.matches("[a-zA-Z0-9-_ áéíóúÁÉÍÓÚ]{1,50}");
    }
}
