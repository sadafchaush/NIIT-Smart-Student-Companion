
package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.UserRepository;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =====================================================
    // GET ALL STUDENTS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {

        return ResponseEntity.ok(
                studentRepository.findAll()
        );
    }


    // =====================================================
    // GET STUDENT BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(
            @PathVariable Long id) {

        return studentRepository.findById(id)
                .map(student -> ResponseEntity.ok(student))
                .orElseGet(() ->
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(null)
                );
    }


    // =====================================================
    // GET STUDENT BY EMAIL
    // =====================================================

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getStudentByEmail(
            @PathVariable String email) {

        return studentRepository
                .findByEmail(email)
                .map(student -> ResponseEntity.ok(student))
                .orElseGet(() ->
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(null)
                );
    }


    // =====================================================
    // ADD STUDENT
    //
    // Creates:
    // 1. Student record
    // 2. Login User record
    // =====================================================

    @PostMapping
    public ResponseEntity<?> addStudent(
            @RequestBody Student student) {

        try {

            // -----------------------------------------
            // Validate Name
            // -----------------------------------------

            if (student.getName() == null ||
                    student.getName().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Student name is required");
            }


            // -----------------------------------------
            // Validate Email
            // -----------------------------------------

            if (student.getEmail() == null ||
                    student.getEmail().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Student email is required");
            }

            String email = student.getEmail()
                    .trim()
                    .toLowerCase();

            student.setEmail(email);


            // -----------------------------------------
            // Validate Password
            // -----------------------------------------

            if (student.getPassword() == null ||
                    student.getPassword().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Student password is required");
            }

            if (student.getPassword().length() < 6) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Password must contain at least 6 characters"
                        );
            }


            // -----------------------------------------
            // Check Student Email
            // -----------------------------------------

            if (studentRepository
                    .findByEmail(email)
                    .isPresent()) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(
                                "Student email already exists"
                        );
            }


            // -----------------------------------------
            // Check Login Email
            // -----------------------------------------

            if (userRepository.existsByEmail(email)) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(
                                "Email is already registered"
                        );
            }


            // -----------------------------------------
            // Save Student
            // -----------------------------------------

            Student savedStudent =
                    studentRepository.save(student);


            // -----------------------------------------
            // Create Login User
            // -----------------------------------------

            User user = new User();

            user.setName(
                    savedStudent.getName()
            );

            user.setEmail(email);

            user.setPassword(
                    passwordEncoder.encode(
                            student.getPassword()
                    )
            );

            user.setRole("STUDENT");


            // -----------------------------------------
            // Save Login User
            // -----------------------------------------

            userRepository.save(user);


            // -----------------------------------------
            // Return Student
            // -----------------------------------------

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedStudent);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error while adding student: "
                            + e.getMessage()
                    );
        }
    }


    // =====================================================
    // UPDATE STUDENT
    //
    // Updates:
    // 1. Student record
    // 2. Corresponding User login account
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody Student studentDetails) {

        try {

            Student student =
                    studentRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Student not found with ID: "
                                                    + id
                                    )
                            );


            String oldEmail = student.getEmail();


            // -----------------------------------------
            // Validate Name
            // -----------------------------------------

            if (studentDetails.getName() == null ||
                    studentDetails.getName()
                            .trim()
                            .isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Student name is required"
                        );
            }


            // -----------------------------------------
            // Validate Email
            // -----------------------------------------

            if (studentDetails.getEmail() == null ||
                    studentDetails.getEmail()
                            .trim()
                            .isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Student email is required"
                        );
            }

            String newEmail =
                    studentDetails.getEmail()
                            .trim()
                            .toLowerCase();


            // -----------------------------------------
            // Check Changed Email
            // -----------------------------------------

            if (oldEmail == null ||
                    !oldEmail.equalsIgnoreCase(newEmail)) {

                if (userRepository.existsByEmail(newEmail)) {

                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(
                                    "Email is already registered"
                            );
                }

                if (studentRepository
                        .findByEmail(newEmail)
                        .isPresent()) {

                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(
                                    "Student email already exists"
                            );
                }
            }


            // -----------------------------------------
            // Update Student
            // -----------------------------------------

            student.setName(
                    studentDetails.getName()
            );

            student.setEmail(newEmail);

            student.setMobile(
                    studentDetails.getMobile()
            );

            student.setCourse(
                    studentDetails.getCourse()
            );

            student.setSemester(
                    studentDetails.getSemester()
            );


            // -----------------------------------------
            // Password
            //
            // Only update when supplied.
            // -----------------------------------------

            String newPassword =
                    studentDetails.getPassword();

            if (newPassword != null &&
                    !newPassword.trim().isEmpty()) {

                if (newPassword.length() < 6) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    "Password must contain at least 6 characters"
                            );
                }

                student.setPassword(newPassword);
            }


            // -----------------------------------------
            // Save Student
            // -----------------------------------------

            Student updatedStudent =
                    studentRepository.save(student);


            // -----------------------------------------
            // Find Existing User
            // -----------------------------------------

            User user = null;

            if (oldEmail != null &&
                    !oldEmail.trim().isEmpty()) {

                user = userRepository
                        .findByEmail(
                                oldEmail
                                        .trim()
                                        .toLowerCase()
                        )
                        .orElse(null);
            }


            // -----------------------------------------
            // Create User If Missing
            //
            // Repairs old students that don't have
            // a login account.
            // -----------------------------------------

            if (user == null) {

                user = new User();

                user.setName(
                        updatedStudent.getName()
                );

                user.setEmail(newEmail);

                String password =
                        updatedStudent.getPassword();

                if (password == null ||
                        password.trim().isEmpty()) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    "Password is required to create Student login account"
                            );
                }

                user.setPassword(
                        passwordEncoder.encode(password)
                );

                user.setRole("STUDENT");

            } else {

                // -------------------------------------
                // Update Existing User
                // -------------------------------------

                user.setName(
                        updatedStudent.getName()
                );

                user.setEmail(newEmail);

                user.setRole("STUDENT");


                // Change password only if supplied

                if (newPassword != null &&
                        !newPassword.trim().isEmpty()) {

                    user.setPassword(
                            passwordEncoder.encode(
                                    newPassword
                            )
                    );
                }
            }


            // -----------------------------------------
            // Save User
            // -----------------------------------------

            userRepository.save(user);


            return ResponseEntity.ok(
                    updatedStudent
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error while updating student: "
                                    + e.getMessage()
                    );
        }
    }


    // =====================================================
    // DELETE STUDENT
    //
    // Deletes:
    // 1. Student record
    // 2. Corresponding User login account
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(
            @PathVariable Long id) {

        try {

            Student student =
                    studentRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Student not found with ID: "
                                                    + id
                                    )
                            );


            // -----------------------------------------
            // Delete Login User
            // -----------------------------------------

            if (student.getEmail() != null) {

                userRepository
                        .findByEmail(
                                student.getEmail()
                                        .trim()
                                        .toLowerCase()
                        )
                        .ifPresent(user -> {

                            if ("STUDENT".equalsIgnoreCase(
                                    user.getRole())) {

                                userRepository.delete(user);
                            }
                        });
            }


            // -----------------------------------------
            // Delete Student
            // -----------------------------------------

            studentRepository.delete(student);


            return ResponseEntity.ok(
                    "Student deleted successfully"
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "Error while deleting student: "
                                    + e.getMessage()
                    );
        }
    }
}
