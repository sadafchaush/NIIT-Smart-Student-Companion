package com.niit.studentcompanion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.niit.studentcompanion.entity.Attendance;
import com.niit.studentcompanion.entity.Student;

@Repository
public interface AttendanceRepository
        extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByStudent(Student student);

    List<Attendance> findByStudentStudentId(Long studentId);

    boolean existsByStudentStudentId(Long studentId);

    java.util.List<Attendance> findBySubjectFacultyFacultyId(Integer facultyId);

    java.util.List<Attendance> findByStudentStudentIdAndSubjectFacultyFacultyId(
            Long studentId,
            Integer facultyId);

    boolean existsByStudentStudentIdAndSubjectSubjectIdAndAttendanceDate(
            Long studentId,
            Integer subjectId,
            java.time.LocalDate attendanceDate);

    boolean existsByStudentStudentIdAndSubjectSubjectIdAndAttendanceDateAndAttendanceIdNot(
            Long studentId,
            Integer subjectId,
            java.time.LocalDate attendanceDate,
            Integer attendanceId);
}