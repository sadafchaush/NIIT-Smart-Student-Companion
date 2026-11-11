package com.niit.studentcompanion.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

    @Test
    void acceptsExactlySixCharacters() {
        assertTrue(PasswordPolicy.isValid("123456"));
        assertTrue(PasswordPolicy.isValid("😀😀😀😀😀😀"));
    }

    @Test
    void rejectsAnyOtherLength() {
        assertFalse(PasswordPolicy.isValid(null));
        assertFalse(PasswordPolicy.isValid("12345"));
        assertFalse(PasswordPolicy.isValid("1234567"));
    }
}
