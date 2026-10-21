package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.niit.studentcompanion.entity.Submission;
import com.niit.studentcompanion.service.SubmissionService;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.FacultyRepository;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public SubmissionController(
            SubmissionService submissionService,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository) {

        this.submissionService =
                submissionService;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    // ==============================
    // GET ALL SUBMISSIONS
    // ==============================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<List<Submission>>
    getAllSubmissions(Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(
                isAdmin
                        ? submissionService.getAllSubmissions()
                        : submissionService.getSubmissionsByFaculty(
                                getFacultyId(authentication))
        );
    }


    // ==============================
    // GET SUBMISSION BY ID
    // ==============================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsSubmission(#p0, authentication)")
    public ResponseEntity<Submission>
    getSubmissionById(
            @PathVariable Long id) {

        return submissionService
                .getSubmissionById(id)
                .map(ResponseEntity::ok)
                .orElse(
                    ResponseEntity.notFound()
                            .build()
                );
    }


    // ==============================
    // GET STUDENT SUBMISSIONS
    // ==============================

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY') or @accountAccess.isOwnSubmissionStudent(#p0, authentication)")
    public ResponseEntity<List<Submission>> getStudentSubmissions(
            @PathVariable Long studentId,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));
        return ResponseEntity.ok(
                isAdmin
                        ? submissionService.getSubmissionsByStudent(studentId)
                        : authentication.getAuthorities().stream()
                                .anyMatch(authority ->
                                        "ROLE_FACULTY".equals(authority.getAuthority()))
                                ? submissionService.getSubmissionsByFaculty(
                                        getFacultyId(authentication)).stream()
                                        .filter(submission -> studentId.equals(
                                                submission.getStudentId()))
                                        .toList()
                                : submissionService.getSubmissionsByStudent(studentId)
        );
    }


    // ==============================
    // GET ASSIGNMENT SUBMISSIONS
    // ==============================

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsAssignment(#p0, authentication)")
    public ResponseEntity<List<Submission>>
    getAssignmentSubmissions(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                submissionService
                        .getSubmissionsByAssignment(
                                assignmentId
                        )
        );
    }


    // ==============================
    // SUBMIT ASSIGNMENT
    // ==============================

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Submission> submitAssignment(
            @RequestBody Submission submission,
            Authentication authentication) {
        if (submission.getAssignmentId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Assignment ID is required");
        }

        Long studentId = studentRepository
                .findByEmail(authentication.getName())
                .map(student -> student.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "A student profile is required"));
        submission.setStudentId(studentId);

        Submission savedSubmission =
                submissionService.submitAssignment(submission);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedSubmission);
    }


    // ==============================
    // DELETE SUBMISSION
    // ==============================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsSubmission(#p0, authentication)")
    public ResponseEntity<String>
    deleteSubmission(
            @PathVariable Long id) {

        if (!submissionService
                .getSubmissionById(id)
                .isPresent()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        submissionService
                .deleteSubmission(id);

        return ResponseEntity.ok(
                "Submission deleted successfully"
        );
    }

    private Long getFacultyId(Authentication authentication) {
        return facultyRepository.findByEmailIgnoreCase(authentication.getName())
                .map(faculty -> faculty.getFacultyId().longValue())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "A faculty profile is required"));
    }
}