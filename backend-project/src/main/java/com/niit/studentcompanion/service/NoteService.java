package com.niit.studentcompanion.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.niit.studentcompanion.entity.Note;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.NoteRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final SubjectRepository subjectRepository;

    private final Path uploadFolder = Paths.get("uploads/notes");

    public NoteService(
            NoteRepository noteRepository,
            SubjectRepository subjectRepository) {
        this.noteRepository = noteRepository;
        this.subjectRepository = subjectRepository;

        try {
            Files.createDirectories(uploadFolder);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create upload folder", e);
        }
    }

    private void validateNoteAccess(Long subjectId, Long facultyId, Integer semester) {
        if (subjectId == null) {
            throw new IllegalArgumentException("Subject is required.");
        }

        if (facultyId == null) {
            throw new IllegalArgumentException("Faculty is required.");
        }

        Subject subject = subjectRepository.findById(Math.toIntExact(subjectId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subject not found with id: " + subjectId));

        if (subject.getFaculty() == null || subject.getFaculty().getFacultyId() == null) {
            throw new IllegalArgumentException(
                    "This subject is not assigned to any faculty yet.");
        }

        if (subject.getFaculty().getFacultyId().longValue() != facultyId) {
            throw new IllegalArgumentException(
                    "This faculty is not assigned to the selected subject.");
        }

        if (semester == null) {
            semester = subject.getSemester();
        }

        if (subject.getSemester() != null && semester != null
                && !subject.getSemester().equals(semester)) {
            throw new IllegalArgumentException(
                    "Note semester does not match the subject semester.");
        }
    }

    public Note createNote(Note note) {
        if (note.getSubjectId() != null && note.getFacultyId() != null) {
            validateNoteAccess(note.getSubjectId(), note.getFacultyId(), note.getSemester());
        }
        return noteRepository.save(note);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public List<Note> getNotesBySemester(Integer semester) {
        if (semester == null) {
            return getAllNotes();
        }
        return noteRepository.findBySemester(semester);
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    public List<Note> getNotesBySubject(Long subjectId) {
        return noteRepository.findBySubjectId(subjectId);
    }

    public List<Note> getNotesByFaculty(Long facultyId) {
        return noteRepository.findByFacultyId(facultyId);
    }

    public List<Note> getNotesBySubjectAndFaculty(Long subjectId, Long facultyId) {
        return noteRepository.findBySubjectIdAndFacultyId(subjectId, facultyId);
    }

    public Note updateNote(Long id, Note updatedNote) {
        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Note not found with id: " + id));

        validateNoteAccess(updatedNote.getSubjectId(), updatedNote.getFacultyId(), updatedNote.getSemester());

        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setDescription(updatedNote.getDescription());
        existingNote.setFileUrl(updatedNote.getFileUrl());
        existingNote.setSubjectId(updatedNote.getSubjectId());
        existingNote.setFacultyId(updatedNote.getFacultyId());
        existingNote.setClassName(updatedNote.getClassName());
        existingNote.setSemester(updatedNote.getSemester());

        return noteRepository.save(existingNote);
    }

    public Note uploadFile(
            MultipartFile file,
            String title,
            String description,
            Long subjectId,
            Long facultyId,
            String className,
            Integer semester) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select a file");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new RuntimeException("Invalid file name");
        }

        validateNoteAccess(subjectId, facultyId, semester);

        try {

            // Get file extension
            String fileExtension = "";

            int dotIndex =
                    originalFileName.lastIndexOf(".");

            if (dotIndex > 0) {
                fileExtension =
                        originalFileName.substring(dotIndex);
            }

            // Generate unique file name
            String newFileName =
                    UUID.randomUUID().toString()
                    + fileExtension;

            // Create file path
            Path filePath =
                    uploadFolder.resolve(newFileName);

            // Save physical file
            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING);

            // Create Note
            Note note = new Note();

            note.setTitle(title);

            note.setDescription(description);

            note.setSubjectId(subjectId);

            note.setFacultyId(facultyId);

            // New category fields
            note.setClassName(className);

            note.setSemester(semester);

            // Save actual stored filename
            note.setFileName(newFileName);

            // Save note first
            Note savedNote =
                    noteRepository.save(note);

            // Create download URL
            savedNote.setFileUrl(
                    "/notes/"
                    + savedNote.getId()
                    + "/download");

            return noteRepository.save(savedNote);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not upload file", e);
        }
    }

    // Get Stored File
    public Path getFilePath(Long id) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Note not found with id: " + id));

        String fileName =
                note.getFileName();

        if (fileName == null ||
                fileName.isBlank()) {

            throw new RuntimeException(
                    "No file attached to this note");
        }

        Path filePath =
                uploadFolder.resolve(fileName);

        if (!Files.exists(filePath)) {

            throw new RuntimeException(
                    "File not found");
        }

        return filePath;
    }

    // Delete Note And Stored File
    public void deleteNote(Long id) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Note not found with id: " + id));

        // Delete physical file
        if (note.getFileName() != null &&
                !note.getFileName().isBlank()) {

            try {

                Path filePath =
                        uploadFolder.resolve(
                                note.getFileName());

                Files.deleteIfExists(filePath);

            } catch (IOException e) {

                throw new RuntimeException(
                        "Could not delete stored file", e);
            }
        }

        // Delete database record
        noteRepository.deleteById(id);
    }
}