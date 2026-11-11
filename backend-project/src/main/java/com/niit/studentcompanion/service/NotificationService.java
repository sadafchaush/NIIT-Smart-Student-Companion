package com.niit.studentcompanion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Announcement;
import com.niit.studentcompanion.entity.Assignment;
import com.niit.studentcompanion.entity.Attendance;
import com.niit.studentcompanion.entity.Note;
import com.niit.studentcompanion.entity.Notification;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.NotificationRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            FacultyRepository facultyRepository) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
    }

    @Transactional(readOnly = true)
    public List<Notification> getForStudent(Long studentId) {
        return notificationRepository
                .findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional
    public Notification markRead(Long notificationId, Long studentId) {
        Notification notification = notificationRepository
                .findByIdAndStudentId(notificationId, studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getForFaculty(Long facultyId) {
        return notificationRepository
                .findByFacultyIdOrderByCreatedAtDesc(facultyId);
    }

    @Transactional
    public Notification markFacultyRead(Long notificationId, Long facultyId) {
        Notification notification = notificationRepository
                .findByIdAndFacultyId(notificationId, facultyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void notifyNote(Note note) {
        Integer semester = note.getSemester();
        if (semester == null && note.getSubjectId() != null
                && note.getSubjectId() <= Integer.MAX_VALUE) {
            semester = subjectRepository.findById(note.getSubjectId().intValue())
                    .map(Subject::getSemester)
                    .orElse(null);
        }
        publishToSemester(
                semester,
                "NOTE",
                "New Notes Uploaded",
                "New notes are available: " + display(note.getTitle(), "Study notes"),
                "notes.html");
    }

    @Transactional
    public void notifyAssignment(Assignment assignment) {
        publishToSemester(
                assignment.getSemester(),
                "ASSIGNMENT",
                "Assignment Posted",
                "A new assignment is available: "
                        + display(assignment.getTitle(), "Assignment"),
                "assignment.html");
    }

    @Transactional
    public void notifyAnnouncement(Announcement announcement) {
        publishToSemester(
                announcement.getSemester(),
                "ANNOUNCEMENT",
                "New Announcement",
                display(announcement.getTitle(), "A faculty announcement is available."),
                "announcement.html");
    }

    @Transactional
    public void notifyAttendance(Attendance attendance) {
        if (attendance.getStudent() == null
                || attendance.getStudent().getStudentId() == null) {
            return;
        }
        String subject = attendance.getSubject() == null
                ? "your subject"
                : display(attendance.getSubject().getSubjectName(), "your subject");
        Notification notification = studentNotification(
                attendance.getStudent().getStudentId(),
                "ATTENDANCE",
                "Attendance Updated",
                "Your attendance for " + subject + " on "
                        + attendance.getAttendanceDate() + " is "
                        + attendance.getStatus() + ".",
                "attendance.html");
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyFacultyMessage(
            String recipientEmail,
            String senderName,
            String message) {
        Optional<Student> recipient =
                studentRepository.findByEmailIgnoreCase(recipientEmail);
        if (recipient.isEmpty()) {
            return;
        }
        notificationRepository.save(studentNotification(
                recipient.get().getStudentId(),
                "MESSAGE",
                "New Faculty Message",
                display(senderName, "A faculty member") + ": "
                        + display(message, "You have a new message"),
                "messages.html"));
    }

    @Transactional
    public void notifyStudentMessage(
            String recipientEmail,
            String senderName,
            String message) {
        facultyRepository.findByEmailIgnoreCase(recipientEmail)
                .ifPresent(recipient -> notificationRepository.save(facultyNotification(
                        recipient.getFacultyId().longValue(),
                        "MESSAGE",
                        "New Student Message",
                        display(senderName, "A student") + ": "
                                + display(message, "You have a new message"),
                        "messages.html")));
    }

    @Transactional
    public void notifyAssignmentSubmission(
            Long facultyId,
            String studentName,
            String assignmentTitle) {
        if (facultyId == null) {
            return;
        }
        notificationRepository.save(facultyNotification(
                facultyId,
                "SUBMISSION",
                "Assignment Submitted",
                display(studentName, "A student") + " submitted "
                        + display(assignmentTitle, "an assignment") + ".",
                "faculty-submissions.html"));
    }

    private void publishToSemester(
            Integer semester,
            String type,
            String title,
            String message,
            String targetUrl) {
        if (semester == null) {
            return;
        }
        List<Notification> notifications = studentRepository
                .findBySemesterIn(List.of(semester)).stream()
                .map(student -> studentNotification(
                        student.getStudentId(), type, title, message, targetUrl))
                .toList();
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    private static Notification studentNotification(
            Long studentId,
            String type,
            String title,
            String message,
            String targetUrl) {
        Notification notification = new Notification();
        notification.setStudentId(studentId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(truncate(message, 2000));
        notification.setTargetUrl(targetUrl);
        return notification;
    }

    private static Notification facultyNotification(
            Long facultyId,
            String type,
            String title,
            String message,
            String targetUrl) {
        Notification notification = new Notification();
        notification.setFacultyId(facultyId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(truncate(message, 2000));
        notification.setTargetUrl(targetUrl);
        return notification;
    }

    private static String display(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength
                ? value
                : value.substring(0, maxLength - 3) + "...";
    }
}
