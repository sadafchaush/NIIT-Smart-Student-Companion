package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/submissions")
@CrossOrigin(origins = "*")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(
            SubmissionService submissionService) {

        this.submissionService =
                submissionService;
    }

    // ==============================
    // GET ALL SUBMISSIONS
    // ==============================

    @GetMapping
    public ResponseEntity<List<Submission>>
    getAllSubmissions() {

        return ResponseEntity.ok(
                submissionService
                        .getAllSubmissions()
        );
    }


    // ==============================
    // GET SUBMISSION BY ID
    // ==============================

    @GetMapping("/{id}")
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
    public ResponseEntity<List<Submission>>
    getStudentSubmissions(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                submissionService
                        .getSubmissionsByStudent(
                                studentId
                        )
        );
    }


    // ==============================
    // GET ASSIGNMENT SUBMISSIONS
    // ==============================

    @GetMapping("/assignment/{assignmentId}")
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
    public ResponseEntity<?> submitAssignment(
            @RequestBody Submission submission) {

        try {

            if (submission.getAssignmentId()
                    == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Assignment ID is required"
                        );
            }

            if (submission.getStudentId()
                    == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Student ID is required"
                        );
            }

            if (submission.getFileUrl()
                    == null ||
                submission.getFileUrl()
                        .trim()
                        .isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "File URL is required"
                        );
            }


            Submission savedSubmission =
                    submissionService
                            .submitAssignment(
                                    submission
                            );


            return ResponseEntity.ok(
                    savedSubmission
            );

        }

        catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                        e.getMessage()
                    );
        }
    }


    // ==============================
    // DELETE SUBMISSION
    // ==============================

    @DeleteMapping("/{id}")
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
}