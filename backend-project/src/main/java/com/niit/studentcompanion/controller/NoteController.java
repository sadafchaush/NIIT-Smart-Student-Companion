package com.niit.studentcompanion.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import com.niit.studentcompanion.entity.Note;
import com.niit.studentcompanion.service.NoteService;

@RestController
@RequestMapping("/notes")
@CrossOrigin(origins = "*")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // Get All Notes
    @GetMapping
    public List<Note> getAllNotes(
            @RequestParam(required = false) Integer semester) {
        return noteService.getNotesBySemester(semester);
    }

    // Create Note Using JSON
    @PostMapping
    public Note createNote(@RequestBody Note note) {
        return noteService.createNote(note);
    }

    // Get Note By ID
    @GetMapping("/{id}")
    public Note getNoteById(
            @PathVariable Long id) {

        return noteService.getNoteById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Note not found"));
    }

    // Update Note
    @PutMapping("/{id}")
    public Note updateNote(
            @PathVariable Long id,
            @RequestBody Note note) {

        return noteService.updateNote(id, note);
    }

    // Upload File
    @PostMapping("/upload")
    public Note uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(
                    value = "description",
                    required = false)
            String description,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("facultyId") Long facultyId,
            @RequestParam("className") String className,
            @RequestParam("semester") Integer semester) {

        return noteService.uploadFile(
                file,
                title,
                description,
                subjectId,
                facultyId,
                className,
                semester);
    }

    // Download File
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id) {

        try {

            Path filePath =
                    noteService.getFilePath(id);

            Resource resource =
                    new UrlResource(
                            filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException(
                        "File not found");
            }

            String contentType =
                    Files.probeContentType(filePath);

            if (contentType == null) {
                contentType =
                        "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" +
                            filePath.getFileName() +
                            "\"")
                    .body(resource);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not download file", e);
        }
    }

    // Delete Note And File
    @DeleteMapping("/{id}")
    public String deleteNote(
            @PathVariable Long id) {

        noteService.deleteNote(id);

        return "Note deleted successfully";
    }
}