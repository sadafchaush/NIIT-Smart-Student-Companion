
package com.niit.studentcompanion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =====================================================
    // LOGIN
    // =====================================================

    public User login(String email, String password) {

        // -----------------------------------------
        // Validate email
        // -----------------------------------------

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email is required");
        }


        // -----------------------------------------
        // Validate password
        // -----------------------------------------

        if (password == null || password.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password is required");
        }


        // -----------------------------------------
        // Normalize email
        // -----------------------------------------

        String normalizedEmail =
                email.trim().toLowerCase();


        // -----------------------------------------
        // Find User
        // -----------------------------------------

        User user =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid email or password")
                        );


        // -----------------------------------------
        // Check password
        // -----------------------------------------

        boolean passwordMatches =
                passwordEncoder.matches(
                        password,
                        user.getPassword()
                );


        if (!passwordMatches) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }


        // -----------------------------------------
        // Validate role
        // -----------------------------------------

        if (user.getRole() == null ||
                user.getRole().trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "User role is not configured");
        }


        // -----------------------------------------
        // Normalize role
        // -----------------------------------------

        user.setRole(
                user.getRole()
                        .trim()
                        .toUpperCase()
        );


        return user;
    }
}
