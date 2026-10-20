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
}