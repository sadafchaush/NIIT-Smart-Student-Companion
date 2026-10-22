
package com.niit.studentcompanion.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.UserRepository;
import com.niit.studentcompanion.security.LoginRateLimiter;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;
    private final String dummyPasswordHash;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LoginRateLimiter loginRateLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginRateLimiter = loginRateLimiter;
        this.dummyPasswordHash = passwordEncoder.encode(
                java.util.UUID.randomUUID().toString());
    }

    public User login(String email, String password, String clientAddress) {

        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (password == null || password.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password is required");
        }

        loginRateLimiter.assertAllowed(clientAddress);
        String normalizedEmail =
                email.trim().toLowerCase(java.util.Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        String storedPassword = user == null
                ? dummyPasswordHash
                : user.getPassword();
        boolean passwordMatches = passwordEncoder.matches(password, storedPassword);

        if (user == null
                || !passwordMatches
                || user.getRole() == null
                || user.getRole().trim().isEmpty()
                || "PENDING_STUDENT".equalsIgnoreCase(user.getRole().trim())) {
            loginRateLimiter.recordFailure(clientAddress);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        user.setRole(user.getRole().trim().toUpperCase(java.util.Locale.ROOT));
        loginRateLimiter.clear(clientAddress);

        return user;
    }
}
