package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.niit.studentcompanion.entity.Notification;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.service.NotificationService;

@RestController
@RequestMapping("/notifications")
@PreAuthorize("hasAnyRole('STUDENT', 'FACULTY')")
public class NotificationController {

    private final NotificationService notificationService;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public NotificationController(
            NotificationService notificationService,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository) {
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    @GetMapping
    public List<Notification> getNotifications(Authentication authentication) {
        if (isFaculty(authentication)) {
            Long facultyId = facultyId(authentication);
            return notificationService.getForFaculty(facultyId);
        }
        Long studentId = studentRepository
                .findByEmailIgnoreCase(authentication.getName())
                .map(student -> student.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student account not found"));
        return notificationService.getForStudent(studentId);
    }

    @PatchMapping("/{id}/read")
    public Notification markRead(
            @PathVariable Long id,
            Authentication authentication) {
        if (isFaculty(authentication)) {
            return notificationService.markFacultyRead(
                    id, facultyId(authentication));
        }
        Long studentId = studentRepository
                .findByEmailIgnoreCase(authentication.getName())
                .map(student -> student.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student account not found"));
        return notificationService.markRead(id, studentId);
    }

    private Long facultyId(Authentication authentication) {
        return facultyRepository.findByEmailIgnoreCase(authentication.getName())
                .map(faculty -> faculty.getFacultyId().longValue())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Faculty account not found"));
    }

    private static boolean isFaculty(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_FACULTY".equals(authority.getAuthority()));
    }
}
