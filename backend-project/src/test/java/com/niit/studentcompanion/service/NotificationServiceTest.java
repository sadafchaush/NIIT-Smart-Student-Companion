package com.niit.studentcompanion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Assignment;
import com.niit.studentcompanion.entity.Attendance;
import com.niit.studentcompanion.entity.Notification;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.NotificationRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Test
    void publishesAcademicUpdatesToStudentsInTheSameSemester() {
        Student firstStudent = new Student();
        firstStudent.setStudentId(12L);
        Student secondStudent = new Student();
        secondStudent.setStudentId(13L);
        when(studentRepository.findBySemesterIn(List.of(3)))
                .thenReturn(List.of(firstStudent, secondStudent));
        when(notificationRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Assignment assignment = new Assignment();
        assignment.setTitle("Research report");
        assignment.setSemester(3);

        new NotificationService(
                notificationRepository, studentRepository, subjectRepository,
                facultyRepository)
                .notifyAssignment(assignment);

        ArgumentCaptor<List<Notification>> notifications =
                ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(notifications.capture());
        assertEquals(2, notifications.getValue().size());
        assertEquals(12L, notifications.getValue().get(0).getStudentId());
        assertEquals("Assignment Posted", notifications.getValue().get(0).getTitle());
        assertEquals(
                "A new assignment is available: Research report",
                notifications.getValue().get(0).getMessage());
        assertEquals("assignment.html", notifications.getValue().get(0).getTargetUrl());
    }

    @Test
    void attendanceUpdatesAreSavedOnlyForTheAffectedStudent() {
        Student student = new Student();
        student.setStudentId(27L);
        Subject subject = new Subject();
        subject.setSubjectName("Mathematics");
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSubject(subject);
        attendance.setAttendanceDate(LocalDate.of(2026, 11, 10));
        attendance.setStatus("Present");
        when(notificationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        new NotificationService(
                notificationRepository, studentRepository, subjectRepository,
                facultyRepository)
                .notifyAttendance(attendance);

        verify(notificationRepository).save(argThat(notification ->
                notification.getStudentId().equals(27L)
                        && notification.getTitle().equals("Attendance Updated")
                        && notification.getMessage().contains("Mathematics")
                        && notification.getMessage().contains("Present")
                        && notification.getTargetUrl().equals("attendance.html")));
        verifyNoInteractions(studentRepository);
    }

    @Test
    void facultyMessageNotificationFitsTheStoredMessageLimit() {
        Student student = new Student();
        student.setStudentId(31L);
        when(studentRepository.findByEmailIgnoreCase("student@niit.edu"))
                .thenReturn(Optional.of(student));
        when(notificationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        new NotificationService(
                notificationRepository, studentRepository, subjectRepository,
                facultyRepository)
                .notifyFacultyMessage(
                        "student@niit.edu", "Faculty", "x".repeat(2000));

        verify(notificationRepository).save(argThat(notification ->
                notification.getStudentId().equals(31L)
                        && notification.getMessage().length() == 2000
                        && notification.getTargetUrl().equals("messages.html")));
    }

    @Test
    void notificationCannotBeMarkedReadByAnotherStudent() {
        when(notificationRepository.findByIdAndStudentId(8L, 15L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new NotificationService(
                        notificationRepository, studentRepository, subjectRepository,
                        facultyRepository)
                        .markRead(8L, 15L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void studentMessageCreatesFacultyNotificationForTheRecipient() {
        Faculty faculty = new Faculty();
        faculty.setFacultyId(18);
        when(facultyRepository.findByEmailIgnoreCase("faculty@niit.edu"))
                .thenReturn(Optional.of(faculty));
        when(notificationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        new NotificationService(
                notificationRepository, studentRepository, subjectRepository,
                facultyRepository)
                .notifyStudentMessage(
                        "faculty@niit.edu", "Student A", "Please clarify the task");

        verify(notificationRepository).save(argThat(notification ->
                notification.getStudentId() == null
                        && notification.getFacultyId().equals(18L)
                        && notification.getTitle().equals("New Student Message")
                        && notification.getMessage()
                                .equals("Student A: Please clarify the task")
                        && notification.getTargetUrl().equals("messages.html")));
    }

    @Test
    void assignmentSubmissionNotificationLinksToFacultySubmissions() {
        when(notificationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        new NotificationService(
                notificationRepository, studentRepository, subjectRepository,
                facultyRepository)
                .notifyAssignmentSubmission(22L, "Student B", "Lab report");

        verify(notificationRepository).save(argThat(notification ->
                notification.getFacultyId().equals(22L)
                        && notification.getStudentId() == null
                        && notification.getTitle().equals("Assignment Submitted")
                        && notification.getTargetUrl().equals("faculty-submissions.html")));
    }

    @Test
    void notificationCannotBeMarkedReadByAnotherFacultyMember() {
        when(notificationRepository.findByIdAndFacultyId(8L, 15L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new NotificationService(
                        notificationRepository, studentRepository, subjectRepository,
                        facultyRepository)
                        .markFacultyRead(8L, 15L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
