package com.niit.studentcompanion.service;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.niit.studentcompanion.entity.Attendance;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.Subject;
import com.niit.studentcompanion.repository.AttendanceRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private com.niit.studentcompanion.repository.FacultyRepository facultyRepository;


    // Add Attendance
    public Attendance addAttendance(Attendance attendance) {

        // Find Student
        if (attendance.getStudent() != null
                && attendance.getStudent().getStudentId() != null) {

            Long studentId =
                    attendance.getStudent().getStudentId();

            Student student = studentRepository
                    .findById(studentId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Student not found with ID: "
                                    + studentId));

            attendance.setStudent(student);
        }


        // Find Subject
        if (attendance.getSubject() != null
                && attendance.getSubject().getSubjectId() != null) {

            Integer subjectId =
                    attendance.getSubject().getSubjectId();

            Subject subject = subjectRepository
                    .findById(subjectId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Subject not found with ID: "
                                    + subjectId));

            attendance.setSubject(subject);
        }

        if (attendance.getStudent() == null || attendance.getSubject() == null
                || attendance.getAttendanceDate() == null
                || attendance.getStatus() == null
                || attendance.getStatus().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Student, subject, date, and status are required");
        }
        validateStudentSemester(attendance);

        if (attendanceRepository
                .existsByStudentStudentIdAndSubjectSubjectIdAndAttendanceDate(
                        attendance.getStudent().getStudentId(),
                        attendance.getSubject().getSubjectId(),
                        attendance.getAttendanceDate())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Attendance is already recorded for this student, subject, and date");
        }
        return attendanceRepository.save(attendance);
    }


    // Get All Attendance
    public List<Attendance> getAllAttendance() {

        return attendanceRepository.findAll();
    }

    public List<Attendance> getAttendanceByFaculty(String email) {
        Integer facultyId = facultyRepository.findByEmailIgnoreCase(email)
                .map(faculty -> faculty.getFacultyId())
                .orElse(null);
        return facultyId == null
                ? List.of()
                : attendanceRepository.findBySubjectFacultyFacultyId(facultyId);
    }

    public List<Attendance> getAttendanceByStudentAndFaculty(
            Long studentId,
            String email) {
        Integer facultyId = facultyRepository.findByEmailIgnoreCase(email)
                .map(faculty -> faculty.getFacultyId())
                .orElse(null);
        return facultyId == null
                ? List.of()
                : attendanceRepository
                        .findByStudentStudentIdAndSubjectFacultyFacultyId(
                                studentId, facultyId);
    }


    // Get Attendance By ID
    public Attendance getAttendanceById(Integer id) {

        return attendanceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Attendance not found with ID: "
                                + id));
    }


    // Update Attendance
    public Attendance updateAttendance(
            Integer id,
            Attendance attendanceDetails) {

        Attendance attendance =
                attendanceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Attendance not found with ID: "
                                + id));


        attendance.setAttendanceDate(
                attendanceDetails.getAttendanceDate());

        attendance.setStatus(
                attendanceDetails.getStatus());


        // Update Student
        if (attendanceDetails.getStudent() != null
                && attendanceDetails
                .getStudent()
                .getStudentId() != null) {

            Long studentId =
                    attendanceDetails
                    .getStudent()
                    .getStudentId();

            Student student =
                    studentRepository
                    .findById(studentId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Student not found with ID: "
                                    + studentId));

            attendance.setStudent(student);
        }


        // Update Subject
        if (attendanceDetails.getSubject() != null
                && attendanceDetails
                .getSubject()
                .getSubjectId() != null) {

            Integer subjectId =
                    attendanceDetails
                    .getSubject()
                    .getSubjectId();

            Subject subject =
                    subjectRepository
                    .findById(subjectId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Subject not found with ID: "
                                    + subjectId));

            attendance.setSubject(subject);
        }

        if (attendance.getStudent() == null
                || attendance.getSubject() == null
                || attendance.getAttendanceDate() == null
                || attendance.getStatus() == null
                || attendance.getStatus().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Student, subject, date, and status are required");
        }
        validateStudentSemester(attendance);

        if (attendanceRepository
                .existsByStudentStudentIdAndSubjectSubjectIdAndAttendanceDateAndAttendanceIdNot(
                        attendance.getStudent().getStudentId(),
                        attendance.getSubject().getSubjectId(),
                        attendance.getAttendanceDate(),
                        id)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Attendance is already recorded for this student, subject, and date");
        }

        return attendanceRepository.save(attendance);
    }

    private void validateStudentSemester(Attendance attendance) {
        if (attendance.getSubject().getSemester() != null
                && attendance.getStudent().getSemester() != null
                && !attendance.getSubject().getSemester()
                        .equals(attendance.getStudent().getSemester())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Student semester does not match the attendance subject");
        }
    }
    
 // Get Attendance By Student ID
    public List<Attendance> getAttendanceByStudent(
            Long studentId) {

        return attendanceRepository
                .findByStudentStudentId(studentId);
    }


    // Delete Attendance
    public void deleteAttendance(Integer id) {

        Attendance attendance =
                attendanceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Attendance not found with ID: "
                                + id));

        attendanceRepository.delete(attendance);
    }
}