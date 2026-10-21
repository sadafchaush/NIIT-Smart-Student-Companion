package com.niit.studentcompanion.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Note;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.service.NoteService;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    public NoteController(
            NoteService noteService,
            FacultyRepository facultyRepository,
            StudentRepository studentRepository) {
        this.noteService = noteService;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<Note> getAllNotes(
            @RequestParam(required = false) Integer semester,
            Authentication authentication) {
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_STUDENT")) {
            semester = studentRepository.findByEmail(authentication.getName())
                    .map(student -> student.getSemester())
                    .orElse(null);
            if (semester == null) {
                return List.of();
            }
        }
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_FACULTY")) {
            Long facultyId = facultyRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .map(faculty -> faculty.getFacultyId().longValue())
                    .orElse(null);
            return facultyId == null
                    ? List.of()
                    : noteService.getNotesByFaculty(facultyId);
        }
        return noteService.getNotesBySemester(semester);
    }

    @PostMapping
    public Note createNote(
            @RequestBody Note note,
            Authentication authentication) {
        note.setFacultyId(resolveFacultyId(
                authentication, note.getFacultyId()));
        return noteService.createNote(note);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@accountAccess.canAccessNote(#p0, authentication)")
    public Note getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Note not found"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.isOwnNote(#p0, authentication)")
    public Note updateNote(
            @PathVariable Long id,
            @RequestBody Note note,
            Authentication authentication) {
        Long facultyId = resolveFacultyId(authentication, note.getFacultyId());
        if (facultyId != null) {
            noteService.requireFacultyOwnership(id, facultyId);
            note.setFacultyId(facultyId);
        }
        return noteService.updateNote(id, note);
    }

    @PostMapping("/upload")
    public Note uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam(value = "facultyId", required = false) Long suppliedFacultyId,
            @RequestParam("className") String className,
            @RequestParam("semester") Integer semester,
            Authentication authentication) {
        Long facultyId = resolveFacultyId(authentication, suppliedFacultyId);
        return noteService.uploadFile(
                file, title, description, subjectId, facultyId, className, semester);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("@accountAccess.canAccessNote(#p0, authentication)")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Path filePath = noteService.getFilePath(id);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(filePath.getFileName().toString())
                                    .build()
                                    .toString())
                    .header("X-Content-Type-Options", "nosniff")
                    .body(resource);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not download file",
                    exception);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.isOwnNote(#p0, authentication)")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long id,
            Authentication authentication) {
        Long facultyId = facultyRepository
                .findByEmailIgnoreCase(authentication.getName())
                .map(faculty -> faculty.getFacultyId().longValue())
                .orElse(null);
        if (facultyId != null) {
            noteService.requireFacultyOwnership(id, facultyId);
        }
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveFacultyId(Authentication authentication, Long suppliedFacultyId) {
        return facultyRepository.findByEmailIgnoreCase(authentication.getName())
                .map(faculty -> faculty.getFacultyId().longValue())
                .orElse(suppliedFacultyId);
    }
}
