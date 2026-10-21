package com.niit.studentcompanion.service;

import com.niit.studentcompanion.entity.Syllabus;
import com.niit.studentcompanion.repository.SyllabusRepository;
import com.niit.studentcompanion.repository.SubjectRepository;
import com.niit.studentcompanion.entity.Subject;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SyllabusService {

    private final SyllabusRepository syllabusRepository;
    private final SubjectRepository subjectRepository;

    public SyllabusService(
            SyllabusRepository syllabusRepository,
            SubjectRepository subjectRepository) {
        this.syllabusRepository = syllabusRepository;
        this.subjectRepository = subjectRepository;
    }

    // Get all syllabus
    public List<Syllabus> getAllSyllabus() {
        return syllabusRepository.findAll();
    }

    public List<Syllabus> getSyllabusBySemester(Integer semester) {
        return syllabusRepository.findBySubjectSemester(semester);
    }

    public List<Syllabus> getSyllabusByFaculty(Integer facultyId) {
        return syllabusRepository.findBySubjectFacultyFacultyId(facultyId);
    }

    // Get syllabus by ID
    public Optional<Syllabus> getSyllabusById(Long id) {
        return syllabusRepository.findById(id);
    }

    // Get syllabus by subject
    public List<Syllabus> getSyllabusBySubject(Long subjectId) {
        return syllabusRepository.findBySubjectSubjectId(subjectId);
    }

    // Create syllabus
    public Syllabus createSyllabus(Syllabus syllabus) {
        syllabus.setSyllabusId(null);
        syllabus.setSubject(resolveSubject(syllabus.getSubject()));
        return syllabusRepository.save(syllabus);
    }

    // Update syllabus
    public Syllabus updateSyllabus(
            Long id,
            Syllabus details) {

        Syllabus syllabus =
                syllabusRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Syllabus not found"
                                )
                        );

        syllabus.setTitle(details.getTitle());
        syllabus.setDescription(details.getDescription());
        syllabus.setFileUrl(details.getFileUrl());
        syllabus.setSubject(resolveSubject(details.getSubject()));

        return syllabusRepository.save(syllabus);
    }

    // Delete syllabus
    public void deleteSyllabus(Long id) {

        if (!syllabusRepository.existsById(id)) {
            throw new RuntimeException(
                    "Syllabus not found"
            );
        }

        syllabusRepository.deleteById(id);
    }

    private Subject resolveSubject(Subject suppliedSubject) {
        if (suppliedSubject == null || suppliedSubject.getSubjectId() == null) {
            throw new IllegalArgumentException("A subject is required");
        }
        return subjectRepository.findById(suppliedSubject.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subject not found"));
    }
}