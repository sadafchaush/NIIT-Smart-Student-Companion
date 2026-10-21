package com.niit.studentcompanion.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.service.QuestionPaperService;
import com.niit.studentcompanion.service.QuestionPaperService.Download;
import com.niit.studentcompanion.service.QuestionPaperService.QuestionPaperResponse;

@RestController
@RequestMapping("/question-papers")
public class QuestionPaperController {

    private final QuestionPaperService questionPaperService;
    private final FacultyRepository facultyRepository;
    private final com.niit.studentcompanion.repository.StudentRepository studentRepository;

    public QuestionPaperController(
            QuestionPaperService questionPaperService,
            FacultyRepository facultyRepository,
            com.niit.studentcompanion.repository.StudentRepository studentRepository) {
        this.questionPaperService = questionPaperService;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<QuestionPaperResponse> getAllQuestionPapers(
            Authentication authentication) {
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_STUDENT")) {
            Integer semester = studentRepository.findByEmail(authentication.getName())
                    .map(student -> student.getSemester())
                    .orElse(null);
            return semester == null
                    ? List.of()
                    : questionPaperService.getQuestionPapersBySemester(semester);
        }
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_FACULTY")) {
            Integer facultyId = facultyRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .map(faculty -> faculty.getFacultyId())
                    .orElse(null);
            return facultyId == null
                    ? List.of()
                    : questionPaperService.getQuestionPapersByFaculty(facultyId);
        }
        return questionPaperService.getAllQuestionPapers();
    }

    @PostMapping("/upload")
    public ResponseEntity<QuestionPaperResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectId") Integer subjectId,
            @RequestParam(value = "facultyId", required = false) Integer suppliedFacultyId,
            @RequestParam("examYear") Integer examYear,
            @RequestParam("examType") String examType,
            Authentication authentication) {
        Integer facultyId = facultyRepository
                .findByEmailIgnoreCase(authentication.getName())
                .map(faculty -> faculty.getFacultyId())
                .orElse(suppliedFacultyId);
        if (facultyId == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "A faculty account is required");
        }
        QuestionPaperResponse result = questionPaperService.upload(
                file, subjectId, facultyId, examYear, examType);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}/download")
    @org.springframework.security.access.prepost.PreAuthorize(
            "@accountAccess.canAccessQuestionPaper(#p0, authentication)")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Download download = questionPaperService.getDownload(id);
        try {
            Resource resource = new UrlResource(download.path().toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(download.fileName())
                                    .build()
                                    .toString())
                    .header("X-Content-Type-Options", "nosniff")
                    .body(resource);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not download the question paper",
                    exception);
        }
    }
}
