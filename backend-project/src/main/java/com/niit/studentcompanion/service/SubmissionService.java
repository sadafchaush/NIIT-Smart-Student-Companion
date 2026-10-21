package com.niit.studentcompanion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.net.URI;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Assignment;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.Submission;
import com.niit.studentcompanion.repository.AssignmentRepository;
import com.niit.studentcompanion.repository.SubmissionRepository;
import com.niit.studentcompanion.repository.StudentRepository;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            AssignmentRepository assignmentRepository,
            StudentRepository studentRepository) {

        this.submissionRepository =
                submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
    }

    // Get all submissions
    public List<Submission> getAllSubmissions() {

        return submissionRepository.findAll();
    }

    public List<Submission> getSubmissionsByFaculty(Long facultyId) {
        List<Long> assignmentIds = assignmentRepository.findByFacultyId(facultyId)
                .stream()
                .map(Assignment::getId)
                .toList();
        return assignmentIds.isEmpty()
                ? List.of()
                : submissionRepository.findByAssignmentIdIn(assignmentIds);
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
        Assignment assignment = assignmentRepository
                .findById(submission.getAssignmentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found"));
        Student student = studentRepository
                .findById(submission.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found"));
        if (assignment.getSemester() != null && student.getSemester() != null
                && !assignment.getSemester().equals(student.getSemester())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This assignment is not available to your semester");
        }
        validateFileUrl(submission.getFileUrl());

        Optional<Submission> existingSubmission =
                submissionRepository
                        .findByAssignmentIdAndStudentId(
                                submission.getAssignmentId(),
                                submission.getStudentId()
                        );

        if (existingSubmission.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already submitted this assignment");
        }

        submission.setSubmittedAt(
                LocalDateTime.now()
        );

        return submissionRepository.save(
                submission
        );
    }

    private static void validateFileUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A valid assignment URL is required");
        }
        try {
            URI uri = URI.create(fileUrl.trim());
            if (!Set.of("http", "https").contains(
                    uri.getScheme() == null
                            ? ""
                            : uri.getScheme().toLowerCase())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Assignment URLs must use HTTP or HTTPS");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A valid assignment URL is required");
        }
    }

    // Delete submission
    public void deleteSubmission(Long id) {

        submissionRepository.deleteById(id);
    }
}