package com.niit.studentcompanion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.QuestionPaperRepository;
import com.niit.studentcompanion.repository.SubjectRepository;
import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.QuestionPaper;
import com.niit.studentcompanion.entity.Subject;

@ExtendWith(MockitoExtension.class)
class QuestionPaperServiceTest {

    @TempDir
    private Path uploadFolder;

    @Mock
    private QuestionPaperRepository questionPaperRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Test
    void rejectsPdfExtensionWhenTheFileSignatureIsNotPdf() {
        QuestionPaperService service = new QuestionPaperService(
                questionPaperRepository, subjectRepository, facultyRepository,
                uploadFolder);
        MockMultipartFile file = new MockMultipartFile(
                "file", "paper.pdf", "application/pdf", "not a pdf".getBytes());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.upload(file, 1, 1, 2025, "Final"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(subjectRepository, facultyRepository, questionPaperRepository);
    }

    @Test
    void rejectsEmptyQuestionPapers() {
        QuestionPaperService service = new QuestionPaperService(
                questionPaperRepository, subjectRepository, facultyRepository,
                uploadFolder);
        MockMultipartFile file = new MockMultipartFile(
                "file", "paper.pdf", "application/pdf", new byte[0]);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.upload(file, 1, 1, 2025, "Final"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(subjectRepository, facultyRepository, questionPaperRepository);
    }

    @Test
    void storesValidatedPdfAndReturnsDownloadEndpoint() throws Exception {
        Faculty faculty = new Faculty();
        faculty.setFacultyId(7);
        Subject subject = new Subject();
        subject.setSubjectId(2);
        subject.setSubjectName("Networks");
        subject.setSemester(3);
        subject.setFaculty(faculty);
        when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));
        when(facultyRepository.findById(7)).thenReturn(Optional.of(faculty));
        when(questionPaperRepository.saveAndFlush(any(QuestionPaper.class)))
                .thenAnswer(invocation -> {
                    QuestionPaper paper = invocation.getArgument(0);
                    paper.setId(11L);
                    return paper;
                });
        QuestionPaperService service = new QuestionPaperService(
                questionPaperRepository, subjectRepository, facultyRepository,
                uploadFolder);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "paper.pdf",
                "application/pdf",
                "%PDF-1.7\nsample".getBytes());

        var paper = service.upload(file, 2, 7, 2025, "Final");

        assertEquals("Networks", paper.subject());
        assertEquals(3, paper.semester());
        assertEquals("/question-papers/11/download", paper.file());
        try (var files = Files.list(uploadFolder)) {
            assertEquals(1, files.count());
        }
    }
}
