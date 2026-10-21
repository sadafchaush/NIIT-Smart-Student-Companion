package com.niit.studentcompanion.dto;

import com.niit.studentcompanion.entity.Student;

public record StudentResponse(
        Long studentId,
        String name,
        String email,
        String mobile,
        String course,
        Integer semester) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getStudentId(),
                student.getName(),
                student.getEmail(),
                student.getMobile(),
                student.getCourse(),
                student.getSemester());
    }
}
