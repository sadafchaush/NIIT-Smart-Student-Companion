package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
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
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.dto.SubjectResponse;

@RestController
@RequestMapping("/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    // Add Subject
    @PostMapping
    public Subject addSubject(
            @RequestBody Subject subject) {

        return subjectService.addSubject(subject);
    }


    // Get All Subjects
    @GetMapping
    public List<SubjectResponse> getAllSubjects(Authentication authentication) {
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_STUDENT")) {
            Integer semester = studentRepository.findByEmail(authentication.getName())
                    .map(student -> student.getSemester())
                    .orElse(null);
            return (semester == null
                    ? List.<Subject>of()
                    : subjectService.getSubjectsBySemester(semester))
                    .stream()
                    .map(SubjectResponse::from)
                    .toList();
        }
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_FACULTY")) {
            Integer facultyId = facultyRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .map(faculty -> faculty.getFacultyId())
                    .orElse(null);
            return facultyId == null
                    ? List.of()
                    : subjectService.getSubjectsByFaculty(facultyId).stream()
                            .map(SubjectResponse::from)
                            .toList();
        }
        return subjectService.getAllSubjects().stream()
                .map(SubjectResponse::from)
                .toList();
    }


    // Get Subject By ID
    @GetMapping("/{id}")
    @PreAuthorize("@accountAccess.canAccessSubject(#p0, authentication)")
    public SubjectResponse getSubjectById(
            @PathVariable Integer id) {

        return SubjectResponse.from(subjectService.getSubjectById(id));
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