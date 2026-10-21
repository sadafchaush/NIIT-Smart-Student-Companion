package com.niit.studentcompanion.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Note;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.NoteRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final SubjectRepository subjectRepository;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final Path uploadFolder = Paths.get("uploads", "notes")
            .toAbsolutePath().normalize();

    public NoteService(
            NoteRepository noteRepository,
            SubjectRepository subjectRepository) {
        this.noteRepository = noteRepository;
        this.subjectRepository = subjectRepository;

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
        validateNoteAccess(note.getSubjectId(), note.getFacultyId(), note.getSemester());
        note.setFileName(null);
        note.setFileUrl(null);
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

    public void requireFacultyOwnership(Long id, Long facultyId) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Note not found"));
        if (note.getFacultyId() == null
                || !note.getFacultyId().equals(facultyId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You can only manage your own notes");
        }
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
        existingNote.setSubjectId(updatedNote.getSubjectId());
        existingNote.setFacultyId(updatedNote.getFacultyId());
        existingNote.setClassName(updatedNote.getClassName());
        existingNote.setSemester(updatedNote.getSemester());

        return noteRepository.save(existingNote);
    }

    @Transactional
    public Note uploadFile(
            MultipartFile file,
            String title,
            String description,
            Long subjectId,
            Long facultyId,
            String className,
            Integer semester) {

        String extension = validateFile(file);
        validateNoteAccess(subjectId, facultyId, semester);

        String newFileName = UUID.randomUUID() + extension;
        Path filePath = uploadFolder.resolve(newFileName).normalize();
        try {
            Files.createDirectories(uploadFolder);
            try (var input = file.getInputStream()) {
                Files.copy(input, filePath);
            }

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
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not upload file",
                    e);
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
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

        Path filePath = uploadFolder.resolve(fileName).normalize();

        if (!filePath.startsWith(uploadFolder)
                || !Files.isRegularFile(filePath)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "File not found");
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

                Path filePath = uploadFolder.resolve(
                        note.getFileName()).normalize();
                if (!filePath.startsWith(uploadFolder)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Invalid stored file path");
                }

                Files.deleteIfExists(filePath);

            } catch (IOException e) {

                throw new RuntimeException(
                        "Could not delete stored file", e);
            }
        }

        // Delete database record
        noteRepository.deleteById(id);
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please select a file");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE, "Maximum file size is 10 MB");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        String lowerName = originalName.toLowerCase(Locale.ROOT);
        String extension = lowerName.substring(lowerName.lastIndexOf('.') + 1);
        if (!List.of("pdf", "doc", "docx", "ppt", "pptx",
                "txt", "xls", "xlsx").contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unsupported file type");
        }

        try (var input = file.getInputStream()) {
            byte[] signature = input.readNBytes(8);
            boolean valid = switch (extension) {
                case "pdf" -> signature.length >= 5
                        && signature[0] == '%'
                        && signature[1] == 'P'
                        && signature[2] == 'D'
                        && signature[3] == 'F'
                        && signature[4] == '-';
                case "doc", "ppt", "xls" -> signature.length >= 8
                        && signature[0] == (byte) 0xD0
                        && signature[1] == (byte) 0xCF
                        && signature[2] == 0x11
                        && signature[3] == (byte) 0xE0;
                case "docx", "pptx", "xlsx" -> signature.length >= 4
                        && signature[0] == 'P'
                        && signature[1] == 'K'
                        && signature[2] == 3
                        && signature[3] == 4;
                case "txt" -> {
                    byte[] content = file.getBytes();
                    String text = new String(content, StandardCharsets.UTF_8);
                    yield !text.contains("\u0000")
                            && text.getBytes(StandardCharsets.UTF_8).length == content.length;
                }
                default -> false;
            };
            if (!valid) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "File content does not match its extension");
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read the uploaded file", exception);
        }
        return "." + extension;
    }
}