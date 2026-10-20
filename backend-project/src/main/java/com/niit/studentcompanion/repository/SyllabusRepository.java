package com.niit.studentcompanion.repository;

import com.niit.studentcompanion.entity.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusRepository
        extends JpaRepository<Syllabus, Long> {

    // Find syllabus by subject ID
    List<Syllabus> findBySubjectSubjectId(Long subjectId);

}