package com.niit.studentcompanion.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.UserRepository;

@Component
public class LegacyPasswordMigration implements ApplicationRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;

    public LegacyPasswordMigration(
            UserRepository userRepository,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        userRepository.findAll().stream()
                .filter(user -> !isBcryptHash(user.getPassword()))
                .forEach(this::hashUserPassword);
        studentRepository.findAll().stream()
                .filter(student -> !isBcryptHash(student.getPassword()))
                .forEach(this::hashStudentPassword);
        facultyRepository.findAll().stream()
                .filter(faculty -> !isBcryptHash(faculty.getPassword()))
                .forEach(this::hashFacultyPassword);
    }

    private void hashUserPassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    private void hashStudentPassword(Student student) {
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        studentRepository.save(student);
    }

    private void hashFacultyPassword(Faculty faculty) {
        faculty.setPassword(passwordEncoder.encode(faculty.getPassword()));
        facultyRepository.save(faculty);
    }

    private boolean isBcryptHash(String password) {
        return password != null
                && (password.startsWith("$2a$")
                        || password.startsWith("$2b$")
                        || password.startsWith("$2y$"));
    }
}
