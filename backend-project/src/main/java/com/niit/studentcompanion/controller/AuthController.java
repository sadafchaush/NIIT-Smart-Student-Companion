package com.niit.studentcompanion.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository,
            SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest credentials,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (credentials.email() == null || credentials.password() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required");
        }

        User user = authService.login(
                credentials.email(),
                credentials.password(),
                request.getRemoteAddr());
        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("authenticatedUserId", user.getId());

        String role = user.getRole().trim().toUpperCase(Locale.ROOT);
        var authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        Long studentId = studentRepository.findByEmailIgnoreCase(user.getEmail())
                .map(student -> student.getStudentId())
                .orElse(null);
        Integer facultyId = facultyRepository
                .findByEmailIgnoreCase(user.getEmail())
                .map(faculty -> faculty.getFacultyId())
                .orElse(null);

        return new LoginResponse(
                "Login successful",
                user.getId(),
                user.getName(),
                user.getEmail(),
                role,
                studentId,
                facultyId);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(
            String message,
            Long id,
            String name,
            String email,
            String role,
            Long studentId,
            Integer facultyId) {
    }
}
