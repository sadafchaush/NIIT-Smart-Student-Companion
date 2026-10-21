package com.niit.studentcompanion.dto;

import java.time.LocalDate;

import com.niit.studentcompanion.entity.Attendance;

public record AttendanceResponse(
        Integer attendanceId,
        StudentSummary student,
        SubjectSummary subject,
        LocalDate attendanceDate,
        String status) {

    public static AttendanceResponse from(Attendance attendance) {
        StudentSummary student = attendance.getStudent() == null
                ? null
                : new StudentSummary(
                        attendance.getStudent().getStudentId(),
                        attendance.getStudent().getName(),
                        attendance.getStudent().getSemester());
        SubjectSummary subject = attendance.getSubject() == null
                ? null
                : new SubjectSummary(
                        attendance.getSubject().getSubjectId(),
                        attendance.getSubject().getSubjectName(),
                        attendance.getSubject().getSemester());
        return new AttendanceResponse(
                attendance.getAttendanceId(),
                student,
                subject,
                attendance.getAttendanceDate(),
                attendance.getStatus());
    }

    public record StudentSummary(
            Long studentId,
            String name,
            Integer semester) {
    }

    public record SubjectSummary(
            Integer subjectId,
            String subjectName,
            Integer semester) {
    }
}
