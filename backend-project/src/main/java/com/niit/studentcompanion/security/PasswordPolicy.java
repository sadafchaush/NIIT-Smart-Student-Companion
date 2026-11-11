package com.niit.studentcompanion.security;

public final class PasswordPolicy {

    public static final String MESSAGE =
            "Password must be exactly 6 characters.";

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null
                && password.codePointCount(0, password.length()) == 6;
    }
}
