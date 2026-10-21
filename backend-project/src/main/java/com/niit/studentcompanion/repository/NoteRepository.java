package com.niit.studentcompanion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.niit.studentcompanion.entity.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findBySubjectId(Long subjectId);

    List<Note> findByFacultyId(Long facultyId);

    List<Note> findBySubjectIdAndFacultyId(
            Long subjectId,
            Long facultyId);

    List<Note> findBySemester(Integer semester);

    boolean existsBySubjectId(Long subjectId);

    boolean existsByFacultyId(Long facultyId);
}