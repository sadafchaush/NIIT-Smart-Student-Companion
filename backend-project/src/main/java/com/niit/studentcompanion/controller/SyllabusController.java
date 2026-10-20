package com.niit.studentcompanion.controller;

import com.niit.studentcompanion.entity.Syllabus;
import com.niit.studentcompanion.service.SyllabusService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/syllabus")
@CrossOrigin(origins = "*")
public class SyllabusController {

    private final SyllabusService syllabusService;

    public SyllabusController(
            SyllabusService syllabusService) {

        this.syllabusService = syllabusService;
    }


    // ==========================================
    // GET ALL SYLLABUS
    // GET /syllabus
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Syllabus>>
    getAllSyllabus() {

        return ResponseEntity.ok(
                syllabusService.getAllSyllabus()
        );
    }


    // ==========================================
    // GET SYLLABUS BY ID
    // GET /syllabus/{id}
    // ==========================================

    @GetMapping("/{id}")
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