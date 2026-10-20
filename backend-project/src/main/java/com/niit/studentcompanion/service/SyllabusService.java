package com.niit.studentcompanion.service;

import com.niit.studentcompanion.entity.Syllabus;
import com.niit.studentcompanion.repository.SyllabusRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SyllabusService {

    private final SyllabusRepository syllabusRepository;

    public SyllabusService(SyllabusRepository syllabusRepository) {
        this.syllabusRepository = syllabusRepository;
    }

    // Get all syllabus
    public List<Syllabus> getAllSyllabus() {
        return syllabusRepository.findAll();
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
        syllabus.setSubject(details.getSubject());

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
}