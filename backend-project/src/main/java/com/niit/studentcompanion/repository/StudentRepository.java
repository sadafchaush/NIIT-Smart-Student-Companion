package com.niit.studentcompanion.repository;

import com.niit.studentcompanion.entity.Student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    java.util.List<Student> findByEmailIn(Collection<String> emails);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    boolean existsByEmailAndStudentIdNot(String email, Long studentId);

    boolean existsByMobileAndStudentIdNot(String mobile, Long studentId);

    java.util.List<Student> findBySemesterIn(java.util.Collection<Integer> semesters);
}