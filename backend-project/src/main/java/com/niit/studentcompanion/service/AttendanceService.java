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


        return attendanceRepository.save(attendance);
    }


    // Get All Attendance
    public List<Attendance> getAllAttendance() {

        return attendanceRepository.findAll();
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


        return attendanceRepository.save(attendance);
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