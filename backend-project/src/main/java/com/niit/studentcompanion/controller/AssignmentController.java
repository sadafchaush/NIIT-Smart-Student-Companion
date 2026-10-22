package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Assignment;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.service.AssignmentService;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    public AssignmentController(
            AssignmentService assignmentService,
            FacultyRepository facultyRepository,
            StudentRepository studentRepository) {
        this.assignmentService = assignmentService;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(
            @RequestBody Assignment assignment,
            Authentication authentication) {
        assignment.setFacultyId(resolveFacultyId(
                authentication, assignment.getFacultyId()));
        Assignment savedAssignment =
                assignmentService.createAssignment(assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAssignment);
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> getAllAssignments(
            @RequestParam(required = false) Integer semester,
            Authentication authentication) {
        if (isStudent(authentication)) {
            semester = studentRepository.findByEmail(authentication.getName())
                    .map(student -> student.getSemester())
                    .orElse(null);
            if (semester == null) {
                return ResponseEntity.ok(List.of());
            }
        }
        if (isFaculty(authentication)) {
            Long facultyId = facultyRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .map(faculty -> faculty.getFacultyId().longValue())
                    .orElse(null);
            return ResponseEntity.ok(facultyId == null
                    ? List.of()
                    : assignmentService.getAssignmentsByFaculty(facultyId));
        }
        return ResponseEntity.ok(
                assignmentService.getAssignmentsBySemester(semester));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accountAccess.canAccessAssignment(#p0, authentication)")
    public ResponseEntity<?> getAssignmentById(
            @PathVariable Long id) {
        Assignment assignment = assignmentService.getAssignmentById(id)
                .orElse(null);
        if (assignment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiErrorResponse.of(
                            HttpStatus.NOT_FOUND,
                            "Assignment not found."));
        }
        return ResponseEntity.ok(assignment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsAssignment(#p0, authentication)")
    public ResponseEntity<Assignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody Assignment assignment,
            Authentication authentication) {
        assignment.setFacultyId(resolveFacultyId(
                authentication, assignment.getFacultyId()));
        return ResponseEntity.ok(
                assignmentService.updateAssignment(id, assignment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsAssignment(#p0, authentication)")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveFacultyId(
            Authentication authentication,
            Long suppliedFacultyId) {
        return facultyRepository.findByEmailIgnoreCase(authentication.getName())
                .map(faculty -> faculty.getFacultyId().longValue())
                .orElseGet(() -> {
                    if (suppliedFacultyId == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Faculty is required");
                    }
                    return suppliedFacultyId;
                });
    }

    private static boolean isStudent(Authentication authentication) {
        return AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_STUDENT");
    }

    private static boolean isFaculty(Authentication authentication) {
        return AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_FACULTY");
    }
}
