package com.niit.studentcompanion.security;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {

    public static final String MESSAGE =
            "Password must be at least 12 characters, include a letter and a number, and use no more than 72 UTF-8 bytes.";

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        if (password == null
                || password.codePointCount(0, password.length()) < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return false;
        }

        boolean hasLetter = password.codePoints().anyMatch(Character::isLetter);
        boolean hasDigit = password.codePoints().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }
}
