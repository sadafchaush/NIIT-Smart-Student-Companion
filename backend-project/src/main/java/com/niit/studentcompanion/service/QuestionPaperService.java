package com.niit.studentcompanion.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.QuestionPaper;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.QuestionPaperRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@Service
public class QuestionPaperService {

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private final QuestionPaperRepository questionPaperRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final Path uploadFolder;

    @Autowired
    public QuestionPaperService(
            QuestionPaperRepository questionPaperRepository,
            SubjectRepository subjectRepository,
            FacultyRepository facultyRepository) {
        this(questionPaperRepository, subjectRepository, facultyRepository,
                Path.of("uploads", "question-papers"));
    }

    QuestionPaperService(
            QuestionPaperRepository questionPaperRepository,
            SubjectRepository subjectRepository,
            FacultyRepository facultyRepository,
            Path uploadFolder) {
        this.questionPaperRepository = questionPaperRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
        this.uploadFolder = uploadFolder.toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<QuestionPaperResponse> getAllQuestionPapers() {
        return questionPaperRepository.findAllByOrderByExamYearDescUploadedAtDesc()
                .stream()
                .map(QuestionPaperService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionPaperResponse> getQuestionPapersBySemester(Integer semester) {
        return questionPaperRepository
                .findBySubjectSemesterOrderByExamYearDescUploadedAtDesc(semester)
                .stream()
                .map(QuestionPaperService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionPaperResponse> getQuestionPapersByFaculty(Integer facultyId) {
        return questionPaperRepository
                .findByFacultyFacultyIdOrderByExamYearDescUploadedAtDesc(facultyId)
                .stream()
                .map(QuestionPaperService::toResponse)
                .toList();
    }

    @Transactional
    public QuestionPaperResponse upload(
            MultipartFile file,
            Integer subjectId,
            Integer facultyId,
            Integer examYear,
            String examType) {
        validateUpload(file, examYear, examType);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Subject not found"));
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Faculty not found"));
        if (subject.getFaculty() == null
                || !faculty.getFacultyId().equals(
                        subject.getFaculty().getFacultyId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only upload question papers for your assigned subjects");
        }

        String storedName = UUID.randomUUID() + ".pdf";
        Path destination = uploadFolder.resolve(storedName).normalize();
        try {
            Files.createDirectories(uploadFolder);
            try (var input = file.getInputStream()) {
                Files.copy(input, destination);
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save the question paper",
                    exception);
        }

        QuestionPaper paper = new QuestionPaper();
        paper.setSubject(subject);
        paper.setFaculty(faculty);
        paper.setExamYear(examYear);
        paper.setExamType(examType.trim());
        paper.setFileName("question-paper-" + examYear + ".pdf");
        paper.setFilePath(uploadFolder.relativize(destination).toString());
        try {
            return toResponse(questionPaperRepository.saveAndFlush(paper));
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public Download getDownload(Long id) {
        QuestionPaper paper = questionPaperRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question paper not found"));
        Path file = uploadFolder.resolve(paper.getFilePath()).normalize();
        if (!file.startsWith(uploadFolder) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Question paper file not found");
        }
        return new Download(file, paper.getFileName());
    }

    private static void validateUpload(
            MultipartFile file,
            Integer year,
            String examType) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please select a PDF file");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE, "Maximum file size is 10 MB");
        }
        if (year == null || year < 1900 || year > 2100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Exam year must be between 1900 and 2100");
        }
        if (examType == null || examType.isBlank() || examType.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A valid exam type is required");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }
        try (var input = file.getInputStream()) {
            byte[] signature = input.readNBytes(5);
            if (signature.length != 5
                    || signature[0] != '%'
                    || signature[1] != 'P'
                    || signature[2] != 'D'
                    || signature[3] != 'F'
                    || signature[4] != '-') {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "The uploaded file is not a valid PDF");
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read the uploaded file", exception);
        }
    }

    private static QuestionPaperResponse toResponse(QuestionPaper paper) {
        return new QuestionPaperResponse(
                paper.getId(),
                paper.getSubject().getSubjectName(),
                paper.getSubject().getSemester(),
                paper.getExamYear(),
                paper.getExamType(),
                paper.getFileName(),
                "/question-papers/" + paper.getId() + "/download");
    }

    public record QuestionPaperResponse(
            Long id,
            String subject,
            Integer semester,
            Integer year,
            String exam,
            String fileName,
            String file) {
    }

    public record Download(Path path, String fileName) {
    }
}
