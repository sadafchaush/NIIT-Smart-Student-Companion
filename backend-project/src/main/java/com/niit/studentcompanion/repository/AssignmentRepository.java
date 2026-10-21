package com.niit.studentcompanion.repository;

import com.niit.studentcompanion.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    java.util.List<Assignment> findBySemester(Integer semester);

    java.util.List<Assignment> findByFacultyId(Long facultyId);

    java.util.List<Assignment> findByFacultyIdOrderByDueDateAsc(Long facultyId);

    boolean existsBySubjectId(Long subjectId);

    boolean existsByFacultyId(Long facultyId);
}