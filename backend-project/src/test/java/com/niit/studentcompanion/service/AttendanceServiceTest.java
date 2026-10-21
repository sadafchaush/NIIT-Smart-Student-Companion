package com.niit.studentcompanion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Attendance;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.AttendanceRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void rejectsDuplicateAttendanceForTheSameStudentSubjectAndDate() {
        Student student = new Student();
        student.setStudentId(1L);
        Subject subject = new Subject();
        subject.setSubjectId(2);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));
        when(attendanceRepository
                .existsByStudentStudentIdAndSubjectSubjectIdAndAttendanceDate(
                        1L, 2, LocalDate.of(2026, 1, 15)))
                .thenReturn(true);
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSubject(subject);
        attendance.setAttendanceDate(LocalDate.of(2026, 1, 15));
        attendance.setStatus("PRESENT");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> attendanceService.addAttendance(attendance));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verifyNoInteractionsWhenSaveNotCalled();
    }

    private void verifyNoInteractionsWhenSaveNotCalled() {
        org.mockito.Mockito.verify(attendanceRepository,
                org.mockito.Mockito.never()).save(
                        org.mockito.ArgumentMatchers.any());
    }
}
