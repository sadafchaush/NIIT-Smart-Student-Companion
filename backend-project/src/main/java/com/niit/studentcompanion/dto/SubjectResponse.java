package com.niit.studentcompanion.dto;

import com.niit.studentcompanion.entity.Subject;

public record SubjectResponse(
        Integer subjectId,
        String subjectName,
        String subjectCode,
        Integer semester,
        FacultySummary faculty) {

    public static SubjectResponse from(Subject subject) {
        FacultySummary faculty = subject.getFaculty() == null
                ? null
                : new FacultySummary(
                        subject.getFaculty().getFacultyId(),
                        subject.getFaculty().getFullName(),
                        subject.getFaculty().getEmail());
        return new SubjectResponse(
                subject.getSubjectId(),
                subject.getSubjectName(),
                subject.getSubjectCode(),
                subject.getSemester(),
                faculty);
    }

    public record FacultySummary(
            Integer facultyId,
            String fullName,
            String email) {
    }
}
