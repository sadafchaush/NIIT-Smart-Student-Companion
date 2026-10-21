package com.niit.studentcompanion.dto;

import java.time.LocalDateTime;

import com.niit.studentcompanion.entity.Faculty;

public record FacultyResponse(
        Integer facultyId,
        String fullName,
        String email,
        String phone,
        String department,
        String designation,
        LocalDateTime createdAt) {

    public static FacultyResponse from(Faculty faculty) {
        return new FacultyResponse(
                faculty.getFacultyId(),
                faculty.getFullName(),
                faculty.getEmail(),
                faculty.getPhone(),
                faculty.getDepartment(),
                faculty.getDesignation(),
                faculty.getCreatedAt());
    }
}
