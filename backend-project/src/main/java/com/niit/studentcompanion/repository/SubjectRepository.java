package com.niit.studentcompanion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.niit.studentcompanion.entity.Subject;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    java.util.List<Subject> findBySemester(Integer semester);

    java.util.List<Subject> findByFacultyFacultyId(Integer facultyId);
}