package com.niit.studentcompanion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;


    // =====================================================
    // REGISTER NEW STUDENT
    // =====================================================

    public Student registerStudent(Student student) {

        validateStudent(student);

        // Email duplicate check
        if (repository.existsByEmail(student.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Mobile duplicate check
        if (repository.existsByMobile(student.getMobile())) {
            throw new RuntimeException("Mobile number already exists");
        }

        return repository.save(student);
    }


    // =====================================================
    // CREATE / SAVE STUDENT
    // =====================================================

    public Student saveStudent(Student student) {

        validateStudent(student);

        // New student
        if (student.getStudentId() == null) {

            if (repository.existsByEmail(student.getEmail())) {
                throw new RuntimeException("Email already exists");
            }

            if (repository.existsByMobile(student.getMobile())) {
                throw new RuntimeException("Mobile number already exists");
            }

        } else {

            // Existing student / Edit
            if (repository.existsByEmailAndStudentIdNot(
                    student.getEmail(),
                    student.getStudentId())) {

                throw new RuntimeException("Email already exists");
            }

            if (repository.existsByMobileAndStudentIdNot(
                    student.getMobile(),
                    student.getStudentId())) {

                throw new RuntimeException("Mobile number already exists");
            }
        }

        return repository.save(student);
    }


    // =====================================================
    // READ ALL STUDENTS
    // =====================================================

    public List<Student> getAllStudents() {

        return repository.findAll();
    }


    // =====================================================
    // READ STUDENT BY ID
    // =====================================================

    public Student getStudentById(Long id) {

        return repository.findById(id).orElse(null);
    }


    // =====================================================
    // UPDATE STUDENT
    // =====================================================

    public Student updateStudent(
            Long id,
            Student updatedStudent) {

        Student existingStudent =
                repository.findById(id).orElse(null);

        if (existingStudent == null) {
            throw new RuntimeException(
                    "Student not found with ID: " + id);
        }


        // ==========================================
        // VALIDATE UPDATED DATA
        // ==========================================

        validateStudent(updatedStudent);


        // ==========================================
        // CHECK DUPLICATE EMAIL
        // Ignore current student's own email
        // ==========================================

        if (repository.existsByEmailAndStudentIdNot(
                updatedStudent.getEmail(),
                id)) {

            throw new RuntimeException(
                    "Email already exists");
        }


        // ==========================================
        // CHECK DUPLICATE MOBILE
        // Ignore current student's own mobile
        // ==========================================

        if (repository.existsByMobileAndStudentIdNot(
                updatedStudent.getMobile(),
                id)) {

            throw new RuntimeException(
                    "Mobile number already exists");
        }


        // ==========================================
        // UPDATE FIELDS
        // ==========================================

        existingStudent.setName(
                updatedStudent.getName());

        existingStudent.setEmail(
                updatedStudent.getEmail());

        existingStudent.setMobile(
                updatedStudent.getMobile());

        existingStudent.setCourse(
                updatedStudent.getCourse());

        existingStudent.setPassword(
                updatedStudent.getPassword());


        // ==========================================
        // SAVE UPDATED STUDENT
        // ==========================================

        return repository.save(existingStudent);
    }


    // =====================================================
    // DELETE STUDENT
    // =====================================================

    public boolean deleteStudent(Long id) {

        if (repository.existsById(id)) {

            repository.deleteById(id);

            return true;
        }

        return false;
    }


    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateStudent(Student student) {

        if (student == null) {
            throw new RuntimeException(
                    "Student data is required");
        }


        // ==========================================
        // NAME
        // ==========================================

        if (student.getName() == null ||
                student.getName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Name is required");
        }

        if (!student.getName()
                .matches("[a-zA-Z ]+")) {

            throw new RuntimeException(
                    "Name can contain only letters and spaces");
        }


        // ==========================================
        // EMAIL
        // ==========================================

        if (student.getEmail() == null ||
                student.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "Email is required");
        }

        if (!student.getEmail()
                .matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new RuntimeException(
                    "Enter a valid email address");
        }


        // ==========================================
        // MOBILE
        // ==========================================

        if (student.getMobile() == null ||
                !student.getMobile()
                    .matches("\\d{10}")) {

            throw new RuntimeException(
                    "Mobile number must contain exactly 10 digits");
        }


        // ==========================================
        // PASSWORD
        // ==========================================

        if (student.getPassword() == null ||
                student.getPassword().length() < 6) {

            throw new RuntimeException(
                    "Password must contain at least 6 characters");
        }


        // ==========================================
        // COURSE
        // ==========================================

        if (student.getCourse() == null ||
                student.getCourse().trim().isEmpty()) {

            throw new RuntimeException(
                    "Course is required");
        }
    }
}