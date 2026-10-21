
package com.niit.studentcompanion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;

import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.UserRepository;
import com.niit.studentcompanion.repository.AttendanceRepository;
import com.niit.studentcompanion.repository.SubmissionRepository;
import com.niit.studentcompanion.repository.DirectMessageRepository;
import com.niit.studentcompanion.dto.StudentResponse;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.SubjectRepository;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private DirectMessageRepository directMessageRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    // =====================================================
    // GET ALL STUDENTS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents(
            Authentication authentication) {
        List<Student> students;
        if (AuthorityUtils.authorityListToSet(authentication.getAuthorities())
                .contains("ROLE_ADMIN")) {
            students = studentRepository.findAll();
        } else {
            Integer facultyId = facultyRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .map(faculty -> faculty.getFacultyId())
                    .orElse(null);
            List<Integer> semesters = facultyId == null
                    ? List.of()
                    : subjectRepository.findByFacultyFacultyId(facultyId).stream()
                            .map(subject -> subject.getSemester())
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .toList();
            students = semesters.isEmpty()
                    ? List.of()
                    : studentRepository.findBySemesterIn(semesters);
        }

        return ResponseEntity.ok(
                students.stream()
                        .map(StudentResponse::from)
                        .toList()
        );
    }


    // =====================================================
    // GET STUDENT BY ID
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.canViewStudentId(#p0, authentication)")
    public ResponseEntity<?> getStudentById(
            @PathVariable Long id) {

        return studentRepository.findById(id)
                .map(student -> ResponseEntity.ok(StudentResponse.from(student)))
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
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.canViewStudentEmail(#p0, authentication)")
    public ResponseEntity<?> getStudentByEmail(
            @PathVariable String email) {

        return studentRepository
                .findByEmail(email)
                .map(student -> ResponseEntity.ok(StudentResponse.from(student)))
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
    @Transactional
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

            if (student.getMobile() == null
                    || !student.getMobile().matches("\\d{10}")) {
                return ResponseEntity.badRequest()
                        .body("Mobile number must contain exactly 10 digits");
            }


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

            if (studentRepository.existsByMobile(student.getMobile())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Mobile number is already registered");
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

            String rawPassword = student.getPassword();
            student.setPassword(passwordEncoder.encode(rawPassword));
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
                            rawPassword
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
                    .body(StudentResponse.from(savedStudent));

        } catch (Exception e) {

            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error while adding student"
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
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
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

            if (studentDetails.getMobile() == null
                    || !studentDetails.getMobile().matches("\\d{10}")) {
                return ResponseEntity.badRequest()
                        .body("Mobile number must contain exactly 10 digits");
            }
            if (studentRepository.existsByMobileAndStudentIdNot(
                    studentDetails.getMobile(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Mobile number is already registered");
            }


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

                student.setPassword(passwordEncoder.encode(newPassword));
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

                    org.springframework.transaction.interceptor.TransactionAspectSupport
                            .currentTransactionStatus().setRollbackOnly();
                    return ResponseEntity
                            .badRequest()
                            .body(
                                    "Password is required to create Student login account"
                            );
                }

                user.setPassword(password);

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


            return ResponseEntity.ok(StudentResponse.from(updatedStudent));

        } catch (Exception e) {

            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error while updating student"
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
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
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

            var account = student.getEmail() == null
                    ? java.util.Optional.<User>empty()
                    : userRepository.findByEmail(
                            student.getEmail().trim().toLowerCase());
            if (attendanceRepository
                    .existsByStudentStudentId(id)
                    || submissionRepository.existsByStudentId(id)
                    || account.filter(user -> directMessageRepository
                            .existsBySender_IdOrRecipient_Id(
                                    user.getId(), user.getId()))
                            .isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Student has attendance, submissions, or messages and cannot be deleted");
            }

            // -----------------------------------------
            // Delete student before its login account so foreign-key
            // failures cannot leave an active account behind.
            // -----------------------------------------

            studentRepository.delete(student);

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
            return ResponseEntity.ok(
                    "Student deleted successfully"
            );

        } catch (Exception e) {

            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "Error while deleting student"
                    );
        }
    }
}
