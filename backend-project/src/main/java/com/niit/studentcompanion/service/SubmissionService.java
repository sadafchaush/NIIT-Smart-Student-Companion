package com.niit.studentcompanion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.niit.studentcompanion.entity.Submission;
import com.niit.studentcompanion.repository.SubmissionRepository;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository) {

        this.submissionRepository =
                submissionRepository;
    }

    // Get all submissions
    public List<Submission> getAllSubmissions() {

        return submissionRepository.findAll();
    }

    // Get submission by ID
    public Optional<Submission> getSubmissionById(
            Long id) {

        return submissionRepository.findById(id);
    }

    // Get submissions by student
    public List<Submission> getSubmissionsByStudent(
            Long studentId) {

        return submissionRepository
                .findByStudentId(studentId);
    }

    // Get submissions by assignment
    public List<Submission> getSubmissionsByAssignment(
            Long assignmentId) {

        return submissionRepository
                .findByAssignmentId(assignmentId);
    }

    // Submit assignment
    public Submission submitAssignment(
            Submission submission) {

        Optional<Submission> existingSubmission =
                submissionRepository
                        .findByAssignmentIdAndStudentId(
                                submission.getAssignmentId(),
                                submission.getStudentId()
                        );

        if (existingSubmission.isPresent()) {

            throw new RuntimeException(
                    "You have already submitted this assignment"
            );
        }

        submission.setSubmittedAt(
                LocalDateTime.now()
        );

        return submissionRepository.save(
                submission
        );
    }

    // Delete submission
    public void deleteSubmission(Long id) {

        submissionRepository.deleteById(id);
    }
}