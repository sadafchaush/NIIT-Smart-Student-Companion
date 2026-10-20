
package com.niit.studentcompanion.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginData,
            HttpServletRequest request) {

        try {

            // -----------------------------------------
            // Get email and password from request
            // -----------------------------------------

            String email =
                    loginData.get("email");

            String password =
                    loginData.get("password");


            // -----------------------------------------
            // Validate input
            // -----------------------------------------

            if (email == null ||
                    email.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Email is required"
                        );
            }


            if (password == null ||
                    password.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Password is required"
                        );
            }


            // -----------------------------------------
            // Authenticate user
            // -----------------------------------------

            User user =
                    authService.login(
                            email,
                            password
                    );

            HttpSession existingSession = request.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }
            request.getSession(true).setAttribute(
                    "authenticatedUserId",
                    user.getId()
            );


            // -----------------------------------------
            // Prepare response
            // -----------------------------------------

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Login successful"
            );

            response.put(
                    "id",
                    user.getId()
            );

            response.put(
                    "name",
                    user.getName()
            );

            response.put(
                    "email",
                    user.getEmail()
            );

            response.put(
                    "role",
                    user.getRole()
            );


            // -----------------------------------------
            // Return successful login
            // -----------------------------------------

            return ResponseEntity.ok(response);


        } catch (RuntimeException e) {

            // -----------------------------------------
            // Invalid login / validation error
            // -----------------------------------------

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                        e.getMessage()
                    );


        } catch (Exception e) {

            // -----------------------------------------
            // Unexpected server error
            // -----------------------------------------

            return ResponseEntity
                    .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                        "Login failed: "
                        + e.getMessage()
                    );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
