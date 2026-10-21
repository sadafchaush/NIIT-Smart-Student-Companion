package com.niit.studentcompanion.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LegacyPasswordMigrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Test
    void hashesLegacyPasswordsInEveryAccountTable() throws Exception {
        User user = new User();
        user.setPassword("user-password");
        Student student = new Student();
        student.setPassword("student-password");
        Faculty faculty = new Faculty();
        faculty.setPassword("faculty-password");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(facultyRepository.findAll()).thenReturn(List.of(faculty));

        LegacyPasswordMigration migration = new LegacyPasswordMigration(
                userRepository,
                studentRepository,
                facultyRepository,
                new BCryptPasswordEncoder());
        migration.run(new DefaultApplicationArguments(new String[0]));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("user-password", user.getPassword()));
        assertTrue(encoder.matches("student-password", student.getPassword()));
        assertTrue(encoder.matches("faculty-password", faculty.getPassword()));
        verify(userRepository).save(user);
        verify(studentRepository).save(student);
        verify(facultyRepository).save(faculty);
    }
}
