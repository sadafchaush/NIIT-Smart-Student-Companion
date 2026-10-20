package com.niit.studentcompanion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.niit.studentcompanion.entity.Assignment;
import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.AssignmentRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
class AccessRulesTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Test
    void studentShouldTrackSemester() {
        Student student = new Student();
        student.setSemester(1);

        assertEquals(1, student.getSemester());
    }

    @Test
    void assignmentShouldRejectFacultyNotAssignedToSubject() {
        Subject subject = new Subject();
        subject.setSubjectId(10);
        subject.setSemester(1);

        Faculty faculty = new Faculty();
        faculty.setFacultyId(5);
        subject.setFaculty(faculty);

        when(subjectRepository.findById(10)).thenReturn(Optional.of(subject));

        AssignmentService assignmentService =
                new AssignmentService(assignmentRepository, subjectRepository);

        Assignment assignment = new Assignment(
                "Semester 1 Assignment",
                "Need to be visible only to semester 1 students",
                LocalDate.now().plusDays(10),
                10L,
                99L
        );

        assertThrows(IllegalArgumentException.class,
                () -> assignmentService.createAssignment(assignment));
    }
}
