package pl.edu.loginsingleton.util;

import pl.edu.loginsingleton.service.AuthException;

public final class InputValidator {

    private static final int USERNAME_MIN = 3;
    private static final int USERNAME_MAX = 20;
    private static final int PASSWORD_MIN = 6;

    private static final String USERNAME_REGEX = "[A-Za-z0-9_]+";

    private InputValidator() {
    }

    public static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new AuthException("Login nie moze byc pusty.");
        }
        String value = username.trim();
        if (value.length() < USERNAME_MIN || value.length() > USERNAME_MAX) {
            throw new AuthException(
                    "Login musi miec od " + USERNAME_MIN + " do " + USERNAME_MAX + " znakow.");
        }
        if (!value.matches(USERNAME_REGEX)) {
            throw new AuthException("Login moze zawierac tylko litery, cyfry i znak '_'.");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new AuthException("Haslo nie moze byc puste.");
        }
        if (password.length() < PASSWORD_MIN) {
            throw new AuthException("Haslo musi miec co najmniej " + PASSWORD_MIN + " znakow.");
        }
    }
}
