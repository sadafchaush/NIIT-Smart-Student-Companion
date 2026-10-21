package com.niit.studentcompanion.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.niit.studentcompanion.entity.Attendance;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.repository.AnnouncementRepository;
import com.niit.studentcompanion.repository.AssignmentRepository;
import com.niit.studentcompanion.repository.AttendanceRepository;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.NoteRepository;
import com.niit.studentcompanion.repository.QuestionPaperRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.SubmissionRepository;
import com.niit.studentcompanion.repository.SubjectRepository;
import com.niit.studentcompanion.repository.SyllabusRepository;

@Component("accountAccess")
public class AccountAccess {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final NoteRepository noteRepository;
    private final SubjectRepository subjectRepository;
    private final AssignmentRepository assignmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubmissionRepository submissionRepository;
    private final SyllabusRepository syllabusRepository;
    private final QuestionPaperRepository questionPaperRepository;

    public AccountAccess(
            StudentRepository studentRepository,
            FacultyRepository facultyRepository,
            NoteRepository noteRepository,
            SubjectRepository subjectRepository,
            AssignmentRepository assignmentRepository,
            AnnouncementRepository announcementRepository,
            AttendanceRepository attendanceRepository,
            SubmissionRepository submissionRepository,
            SyllabusRepository syllabusRepository,
            QuestionPaperRepository questionPaperRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.noteRepository = noteRepository;
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
        this.announcementRepository = announcementRepository;
        this.attendanceRepository = attendanceRepository;
        this.submissionRepository = submissionRepository;
        this.syllabusRepository = syllabusRepository;
        this.questionPaperRepository = questionPaperRepository;
    }

    public boolean isOwnStudentId(Long studentId, Authentication authentication) {
        return authentication != null
                && studentRepository.findByEmail(authentication.getName())
                        .map(student -> student.getStudentId().equals(studentId))
                        .orElse(false);
    }

    public boolean isOwnStudentEmail(String email, Authentication authentication) {
        return authentication != null
                && email != null
                && email.equalsIgnoreCase(authentication.getName())
                && studentRepository.findByEmail(authentication.getName()).isPresent();
    }

    public boolean canViewStudentId(Long studentId, Authentication authentication) {
        if (isAdmin(authentication) || isOwnStudentId(studentId, authentication)) {
            return true;
        }
        Student student = studentRepository.findById(studentId).orElse(null);
        return student != null && canViewStudentSemester(
                student.getSemester(), authentication);
    }

    public boolean canViewStudentEmail(
            String email,
            Authentication authentication) {
        if (isAdmin(authentication) || isOwnStudentEmail(email, authentication)) {
            return true;
        }
        Student student = email == null
                ? null
                : studentRepository.findByEmail(email).orElse(null);
        return student != null && canViewStudentSemester(
                student.getSemester(), authentication);
    }

    public boolean isOwnFacultyId(
            Integer facultyId,
            Authentication authentication) {
        return authentication != null
                && facultyRepository.findByEmailIgnoreCase(authentication.getName())
                        .map(faculty -> faculty.getFacultyId().equals(facultyId))
                        .orElse(false);
    }

    public boolean isOwnNote(Long noteId, Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return facultyId != null
                && noteRepository.findById(noteId)
                        .map(note -> note.getFacultyId() != null
                                && note.getFacultyId().equals(facultyId.longValue()))
                        .orElse(false);
    }

    public boolean canManageSubject(Integer subjectId, Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return facultyId != null
                && subjectRepository.findById(subjectId)
                        .map(subject -> subject.getFaculty() != null
                                && facultyId.equals(
                                        subject.getFaculty().getFacultyId()))
                        .orElse(false);
    }

    public boolean ownsAssignment(Long id, Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return facultyId != null
                && assignmentRepository.findById(id)
                        .map(assignment -> Long.valueOf(facultyId)
                                .equals(assignment.getFacultyId()))
                        .orElse(false);
    }

    public boolean ownsAnnouncement(Long id, Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return facultyId != null
                && announcementRepository.findById(id)
                        .map(announcement -> Long.valueOf(facultyId)
                                .equals(announcement.getFacultyId()))
                        .orElse(false);
    }

    public boolean canManageAttendance(
            Integer id,
            Attendance details,
            Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        if (facultyId == null || details == null || details.getSubject() == null) {
            return false;
        }
        return ownsAttendance(id, facultyId)
                && details.getSubject().getSubjectId() != null
                && subjectRepository.findById(details.getSubject().getSubjectId())
                        .map(subject -> subject.getFaculty() != null
                                && facultyId.equals(
                                        subject.getFaculty().getFacultyId()))
                        .orElse(false);
    }

    public boolean canManageAttendanceRequest(
            Attendance details,
            Authentication authentication) {
        return details != null
                && details.getSubject() != null
                && details.getSubject().getSubjectId() != null
                && canManageSubject(
                        details.getSubject().getSubjectId(),
                        authentication);
    }

    public boolean ownsAttendance(Integer id, Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return facultyId != null && ownsAttendance(id, facultyId);
    }

    public boolean ownsSubmission(Long id, Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return facultyId != null
                && submissionRepository.findById(id)
                        .flatMap(submission ->
                                assignmentRepository.findById(
                                        submission.getAssignmentId()))
                        .map(assignment -> facultyId.longValue()
                                == assignment.getFacultyId())
                        .orElse(false);
    }

    public boolean isOwnSubmissionStudent(
            Long studentId,
            Authentication authentication) {
        return isOwnStudentId(studentId, authentication);
    }

    public boolean canAccessNote(Long id, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        Integer facultyId = facultyId(authentication);
        if (facultyId != null) {
            return noteRepository.findById(id)
                    .map(note -> Long.valueOf(facultyId)
                            .equals(note.getFacultyId()))
                    .orElse(false);
        }
        Integer semester = studentSemester(authentication);
        return semester != null && noteRepository.findById(id)
                .map(note -> semester.equals(note.getSemester()))
                .orElse(false);
    }

    public boolean canAccessAssignment(Long id, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        Integer facultyId = facultyId(authentication);
        if (facultyId != null) {
            return ownsAssignment(id, authentication);
        }
        Integer semester = studentSemester(authentication);
        return semester != null && assignmentRepository.findById(id)
                .map(assignment -> semester.equals(assignment.getSemester()))
                .orElse(false);
    }

    public boolean canAccessAnnouncement(Long id, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        Integer facultyId = facultyId(authentication);
        if (facultyId != null) {
            return ownsAnnouncement(id, authentication);
        }
        Integer semester = studentSemester(authentication);
        return semester != null && announcementRepository.findById(id)
                .map(announcement -> semester.equals(announcement.getSemester()))
                .orElse(false);
    }

    public boolean canAccessSyllabus(Long id, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        Integer facultyId = facultyId(authentication);
        if (facultyId != null) {
            return syllabusRepository.findById(id)
                    .map(syllabus -> syllabus.getSubject() != null
                            && syllabus.getSubject().getFaculty() != null
                            && facultyId.equals(syllabus.getSubject().getFaculty()
                                    .getFacultyId()))
                    .orElse(false);
        }
        Integer semester = studentSemester(authentication);
        return semester != null && syllabusRepository.findById(id)
                .map(syllabus -> syllabus.getSubject() != null
                        && semester.equals(syllabus.getSubject().getSemester()))
                .orElse(false);
    }

    public boolean ownsSyllabus(Long id, Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return facultyId != null && syllabusRepository.findById(id)
                .map(syllabus -> syllabus.getSubject() != null
                        && syllabus.getSubject().getFaculty() != null
                        && facultyId.equals(syllabus.getSubject().getFaculty()
                                .getFacultyId()))
                .orElse(false);
    }

    public boolean canManageSyllabus(
            com.niit.studentcompanion.entity.Syllabus syllabus,
            Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return facultyId != null
                && syllabus != null
                && syllabus.getSubject() != null
                && syllabus.getSubject().getSubjectId() != null
                && canManageSubject(
                        syllabus.getSubject().getSubjectId(), authentication);
    }

    public boolean canAccessQuestionPaper(Long id, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        Integer facultyId = facultyId(authentication);
        if (facultyId != null) {
            return questionPaperRepository.findById(id)
                    .map(paper -> paper.getFaculty() != null
                            && facultyId.equals(paper.getFaculty().getFacultyId()))
                    .orElse(false);
        }
        Integer semester = studentSemester(authentication);
        return semester != null && questionPaperRepository.findById(id)
                .map(paper -> paper.getSubject() != null
                        && semester.equals(paper.getSubject().getSemester()))
                .orElse(false);
    }

    public boolean canAccessSubject(Integer subjectId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        Integer facultyId = facultyId(authentication);
        if (facultyId != null) {
            return subjectRepository.findById(subjectId)
                    .map(subject -> subject.getFaculty() != null
                            && facultyId.equals(
                                    subject.getFaculty().getFacultyId()))
                    .orElse(false);
        }
        Integer semester = studentSemester(authentication);
        return semester != null && subjectRepository.findById(subjectId)
                .map(subject -> semester.equals(subject.getSemester()))
                .orElse(false);
    }

    public boolean canAccessSubject(Long subjectId, Authentication authentication) {
        if (subjectId == null || subjectId > Integer.MAX_VALUE) {
            return false;
        }
        return canAccessSubject(subjectId.intValue(), authentication);
    }

    private boolean ownsAttendance(Integer id, Integer facultyId) {
        return attendanceRepository.findById(id)
                .map(Attendance::getSubject)
                .map(subject -> subject.getFaculty() != null
                        && facultyId.equals(subject.getFaculty().getFacultyId()))
                .orElse(false);
    }

    private Integer facultyId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return facultyRepository.findByEmailIgnoreCase(authentication.getName())
                .map(faculty -> faculty.getFacultyId())
                .orElse(null);
    }

    private Integer studentSemester(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return studentRepository.findByEmail(authentication.getName())
                .map(student -> student.getSemester())
                .orElse(null);
    }

    private boolean canViewStudentSemester(
            Integer semester,
            Authentication authentication) {
        Integer facultyId = facultyId(authentication);
        return semester != null
                && facultyId != null
                && subjectRepository.findByFacultyFacultyId(facultyId).stream()
                        .anyMatch(subject -> semester.equals(subject.getSemester()));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
