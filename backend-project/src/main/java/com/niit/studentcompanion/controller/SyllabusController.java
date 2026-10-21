package com.niit.studentcompanion.controller;

import com.niit.studentcompanion.entity.Syllabus;
import com.niit.studentcompanion.service.SyllabusService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.FacultyRepository;

@RestController
@RequestMapping("/syllabus")
public class SyllabusController {

    private final SyllabusService syllabusService;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public SyllabusController(
            SyllabusService syllabusService,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository) {

        this.syllabusService = syllabusService;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }


    // ==========================================
    // GET ALL SYLLABUS
    // GET /syllabus
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Syllabus>>
    getAllSyllabus(Authentication authentication) {
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_STUDENT")) {
            Integer semester = studentRepository.findByEmail(authentication.getName())
                    .map(student -> student.getSemester())
                    .orElse(null);
            return ResponseEntity.ok(semester == null
                    ? List.of()
                    : syllabusService.getSyllabusBySemester(semester));
        }
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_FACULTY")) {
            Integer facultyId = facultyRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .map(faculty -> faculty.getFacultyId())
                    .orElse(null);
            return ResponseEntity.ok(facultyId == null
                    ? List.of()
                    : syllabusService.getSyllabusByFaculty(facultyId));
        }
        return ResponseEntity.ok(
                syllabusService.getAllSyllabus()
        );
    }


    // ==========================================
    // GET SYLLABUS BY ID
    // GET /syllabus/{id}
    // ==========================================

    @GetMapping("/{id}")
    @PreAuthorize("@accountAccess.canAccessSyllabus(#p0, authentication)")
    public ResponseEntity<Syllabus>
    getSyllabusById(
            @PathVariable Long id) {

        return syllabusService
                .getSyllabusById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }


    // ==========================================
    // GET SYLLABUS BY SUBJECT
    // GET /syllabus/subject/{subjectId}
    // ==========================================

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("@accountAccess.canAccessSubject(#p0, authentication)")
    public ResponseEntity<List<Syllabus>>
    getSyllabusBySubject(
            @PathVariable Long subjectId) {

        return ResponseEntity.ok(
                syllabusService
                        .getSyllabusBySubject(subjectId)
        );
    }


    // ==========================================
    // CREATE SYLLABUS
    // POST /syllabus
    // ==========================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.canManageSyllabus(#p0, authentication)")
    public ResponseEntity<Syllabus>
    createSyllabus(
            @RequestBody Syllabus syllabus) {

        Syllabus saved =
                syllabusService.createSyllabus(
                        syllabus
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }


    // ==========================================
    // UPDATE SYLLABUS
    // PUT /syllabus/{id}
    // ==========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsSyllabus(#p0, authentication)")
    public ResponseEntity<Syllabus>
    updateSyllabus(
            @PathVariable Long id,
            @RequestBody Syllabus syllabus) {

        try {

            Syllabus updated =
                    syllabusService.updateSyllabus(
                            id,
                            syllabus
                    );

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }


    // ==========================================
    // DELETE SYLLABUS
    // DELETE /syllabus/{id}
    // ==========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.ownsSyllabus(#p0, authentication)")
    public ResponseEntity<Void>
    deleteSyllabus(
            @PathVariable Long id) {

        try {

            syllabusService.deleteSyllabus(id);

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }
}