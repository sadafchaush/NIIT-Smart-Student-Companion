package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.service.SubjectService;

@RestController
@RequestMapping("/subjects")
@CrossOrigin(origins = "*")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;


    // Add Subject
    @PostMapping
    public Subject addSubject(
            @RequestBody Subject subject) {

        return subjectService.addSubject(subject);
    }


    // Get All Subjects
    @GetMapping
    public List<Subject> getAllSubjects() {

        return subjectService.getAllSubjects();
    }


    // Get Subject By ID
    @GetMapping("/{id}")
    public Subject getSubjectById(
            @PathVariable Integer id) {

        return subjectService.getSubjectById(id);
    }


    // Update Subject
    @PutMapping("/{id}")
    public Subject updateSubject(
            @PathVariable Integer id,
            @RequestBody Subject subject) {

        return subjectService.updateSubject(
                id,
                subject);
    }


    // Delete Subject
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubject(
            @PathVariable Integer id) {

        subjectService.deleteSubject(id);

        return ResponseEntity.ok(
                "Subject deleted successfully");
    }
}