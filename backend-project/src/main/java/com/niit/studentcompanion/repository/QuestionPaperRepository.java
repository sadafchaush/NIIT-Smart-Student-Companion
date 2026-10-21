package com.niit.studentcompanion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.niit.studentcompanion.entity.QuestionPaper;

public interface QuestionPaperRepository
        extends JpaRepository<QuestionPaper, Long> {

    List<QuestionPaper> findAllByOrderByExamYearDescUploadedAtDesc();

    List<QuestionPaper> findBySubjectSemesterOrderByExamYearDescUploadedAtDesc(
            Integer semester);

    List<QuestionPaper> findByFacultyFacultyIdOrderByExamYearDescUploadedAtDesc(
            Integer facultyId);
}
