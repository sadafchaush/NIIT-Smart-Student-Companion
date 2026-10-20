package com.niit.studentcompanion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.niit.studentcompanion.entity.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {

    // Get all submissions by student
    List<Submission> findByStudentId(Long studentId);

    // Get all submissions for an assignment
    List<Submission> findByAssignmentId(Long assignmentId);

    // Check whether student already submitted assignment
    Optional<Submission> findByAssignmentIdAndStudentId(
            Long assignmentId,
            Long studentId
    );
}