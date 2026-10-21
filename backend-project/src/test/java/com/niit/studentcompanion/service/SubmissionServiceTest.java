package com.niit.studentcompanion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Assignment;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.Submission;
import com.niit.studentcompanion.repository.AssignmentRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Test
    void rejectsSubmissionFromAnotherSemester() {
        Assignment assignment = new Assignment();
        assignment.setId(10L);
        assignment.setSemester(2);
        Student student = new Student();
        student.setStudentId(20L);
        student.setSemester(4);
        when(assignmentRepository.findById(10L))
                .thenReturn(Optional.of(assignment));
        when(studentRepository.findById(20L))
                .thenReturn(Optional.of(student));
        SubmissionService service = new SubmissionService(
                submissionRepository, assignmentRepository, studentRepository);
        Submission submission = new Submission();
        submission.setAssignmentId(10L);
        submission.setStudentId(20L);
        submission.setFileUrl("https://files.example.invalid/work.pdf");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.submitAssignment(submission));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(submissionRepository);
    }

    @Test
    void rejectsUnsafeSubmissionUrlSchemes() {
        Assignment assignment = new Assignment();
        Student student = new Student();
        when(assignmentRepository.findById(10L))
                .thenReturn(Optional.of(assignment));
        when(studentRepository.findById(20L))
                .thenReturn(Optional.of(student));
        SubmissionService service = new SubmissionService(
                submissionRepository, assignmentRepository, studentRepository);
        Submission submission = new Submission();
        submission.setAssignmentId(10L);
        submission.setStudentId(20L);
        submission.setFileUrl("javascript:alert(1)");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.submitAssignment(submission));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(submissionRepository);
    }
}
