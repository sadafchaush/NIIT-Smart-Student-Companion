
package com.niit.studentcompanion.controller;

import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.niit.studentcompanion.security.PasswordPolicy;

@RestController
@RequestMapping("/students")
public class StudentController {

    private static final Logger logger =
            LoggerFactory.getLogger(StudentController.class);

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

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> getPendingStudents() {
        List<String> pendingEmails = userRepository
                .findAllByRoleIgnoreCase("PENDING_STUDENT")
                .stream()
                .map(User::getEmail)
                .filter(java.util.Objects::nonNull)
                .toList();
        List<StudentResponse> pendingStudents = pendingEmails.isEmpty()
                ? List.of()
                : studentRepository.findByEmailIn(pendingEmails).stream()
                        .map(StudentResponse::from)
                        .toList();
        return ResponseEntity.ok(pendingStudents);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.canViewStudentId(#p0, authentication)")
    public ResponseEntity<?> getStudentById(
            @PathVariable Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiErrorResponse.of(
                            HttpStatus.NOT_FOUND,
                            "Student not found"));
        }
        return ResponseEntity.ok(StudentResponse.from(student));
    }


    // =====================================================
    // GET STUDENT BY EMAIL
    // =====================================================

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or @accountAccess.canViewStudentEmail(#p0, authentication)")
    public ResponseEntity<?> getStudentByEmail(
            @PathVariable String email) {
        Student student = studentRepository.findByEmail(email).orElse(null);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiErrorResponse.of(
                            HttpStatus.NOT_FOUND,
                            "Student not found"));
        }
        return ResponseEntity.ok(StudentResponse.from(student));
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
            @RequestBody RegistrationRequest registration) {

        try {
            Student student = new Student();

            // -----------------------------------------
            // Validate Name
            // -----------------------------------------

            if (registration.name() == null ||
                    registration.name().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                "Student name is required"));
            }


            // -----------------------------------------
            // Validate Email
            // -----------------------------------------

            if (registration.email() == null ||
                    registration.email().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                "Student email is required"));
            }

            String email = registration.email()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            student.setName(registration.name().trim());
            student.setEmail(email);

            if (registration.mobile() == null
                    || !registration.mobile().matches("\\d{10}")) {
                return ResponseEntity.badRequest()
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                "Mobile number must contain exactly 10 digits"));
            }
            student.setMobile(registration.mobile());
            if (registration.course() == null
                    || registration.course().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                "Student course is required"));
            }
            student.setCourse(registration.course());


            // -----------------------------------------
            // Validate Password
            // -----------------------------------------

            if (registration.password() == null ||
                    registration.password().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                "Student password is required"));
            }

            if (!PasswordPolicy.isValid(registration.password())) {

                return ResponseEntity
                        .badRequest()
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                PasswordPolicy.MESSAGE));
            }


            // -----------------------------------------
            // Check Student Email
            // -----------------------------------------

            if (studentRepository.existsByEmailIgnoreCase(email)) {
                return registrationAccepted();
            }

            if (studentRepository.existsByMobile(student.getMobile())) {
                return registrationAccepted();
            }

            // -----------------------------------------
            // Check Login Email
            // -----------------------------------------

            if (userRepository.existsByEmailIgnoreCase(email)) {
                return registrationAccepted();
            }


            // -----------------------------------------
            // Save Student
            // -----------------------------------------

            String rawPassword = registration.password();
            String encodedPassword = passwordEncoder.encode(rawPassword);
            student.setPassword(encodedPassword);
            student.setSemester(null);
            Student savedStudent =
                    studentRepository.saveAndFlush(student);


            // -----------------------------------------
            // Create Login User
            // -----------------------------------------

            User user = new User();

            user.setName(
                    savedStudent.getName()
            );

            user.setEmail(email);

            user.setPassword(encodedPassword);

            user.setRole("PENDING_STUDENT");


            // -----------------------------------------
            // Save Login User
            // -----------------------------------------

            userRepository.saveAndFlush(user);


            // -----------------------------------------
            // Return Student
            // -----------------------------------------

            return registrationAccepted();

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            return registrationAccepted();
        } catch (Exception e) {

            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            logger.error("Failed to register student", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiErrorResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "The request could not be completed."));
        }
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> approveStudent(
            @PathVariable Long id,
            @RequestBody ApprovalRequest approval) {
        if (approval == null
                || approval.semester() == null
                || approval.semester() < 1
                || approval.semester() > 6) {
            return ResponseEntity.badRequest()
                    .body(ApiErrorResponse.of(
                            HttpStatus.BAD_REQUEST,
                            "Semester must be between 1 and 6"));
        }

        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiErrorResponse.of(
                            HttpStatus.NOT_FOUND,
                            "Student not found"));
        }
        User user = userRepository.findByEmail(student.getEmail())
                .orElse(null);
        if (user == null || !"PENDING_STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiErrorResponse.of(
                            HttpStatus.CONFLICT,
                            "Student is not awaiting approval"));
        }

        student.setSemester(approval.semester());
        user.setRole("STUDENT");
        studentRepository.save(student);
        userRepository.save(user);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RegistrationRequest(
            String name,
            String email,
            String password,
            String mobile,
            String course) {
    }

    public record ApprovalRequest(Integer semester) {
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

            Student student = studentRepository.findById(id).orElse(null);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiErrorResponse.of(
                                HttpStatus.NOT_FOUND,
                                "Student not found"));
            }


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
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                "Student name is required"));
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
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                "Student email is required"));
            }

            String newEmail =
                    studentDetails.getEmail()
                            .trim()
                            .toLowerCase(Locale.ROOT);

            if (studentDetails.getMobile() == null
                    || !studentDetails.getMobile().matches("\\d{10}")) {
                return ResponseEntity.badRequest()
                        .body(ApiErrorResponse.of(
                                HttpStatus.BAD_REQUEST,
                                "Mobile number must contain exactly 10 digits"));
            }
            if (studentRepository.existsByMobileAndStudentIdNot(
                    studentDetails.getMobile(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiErrorResponse.of(
                                HttpStatus.CONFLICT,
                                "Mobile number is already registered"));
            }


            // -----------------------------------------
            // Check Changed Email
            // -----------------------------------------

            if (oldEmail == null ||
                    !oldEmail.equalsIgnoreCase(newEmail)) {

                if (userRepository.existsByEmailIgnoreCase(newEmail)) {

                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(ApiErrorResponse.of(
                                    HttpStatus.CONFLICT,
                                    "Email is already registered"));
                }

                if (studentRepository.existsByEmailIgnoreCase(newEmail)) {

                    return ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(ApiErrorResponse.of(
                                    HttpStatus.CONFLICT,
                                    "Student email already exists"));
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

            String encodedPassword = null;
            if (newPassword != null && !newPassword.trim().isEmpty()) {

                if (!PasswordPolicy.isValid(newPassword)) {

                    return ResponseEntity
                            .badRequest()
                            .body(ApiErrorResponse.of(
                                    HttpStatus.BAD_REQUEST,
                                    PasswordPolicy.MESSAGE));
                }

                encodedPassword = passwordEncoder.encode(newPassword);
                student.setPassword(encodedPassword);
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
                        .findByEmailIgnoreCase(
                                oldEmail
                                        .trim()
                                        .toLowerCase(Locale.ROOT)
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
                org.springframework.transaction.interceptor.TransactionAspectSupport
                        .currentTransactionStatus().setRollbackOnly();
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiErrorResponse.of(
                                HttpStatus.CONFLICT,
                                "The student login account is missing."));
            }

            user.setName(updatedStudent.getName());
            user.setEmail(newEmail);
            if (encodedPassword != null) {
                user.setPassword(encodedPassword);
            }


            // -----------------------------------------
            // Save User
            // -----------------------------------------

            userRepository.save(user);


            return ResponseEntity.ok(StudentResponse.from(updatedStudent));

        } catch (Exception e) {

            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
            logger.error("Failed to update student", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiErrorResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "The request could not be completed."));
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

            Student student = studentRepository.findById(id).orElse(null);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiErrorResponse.of(
                                HttpStatus.NOT_FOUND,
                                "Student not found"));
            }

            var account = student.getEmail() == null
                    ? java.util.Optional.<User>empty()
                    : userRepository.findByEmailIgnoreCase(
                            student.getEmail().trim().toLowerCase(Locale.ROOT));
            if (attendanceRepository
                    .existsByStudentStudentId(id)
                    || submissionRepository.existsByStudentId(id)
                    || account.filter(user -> directMessageRepository
                            .existsBySender_IdOrRecipient_Id(
                                    user.getId(), user.getId()))
                            .isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiErrorResponse.of(
                                HttpStatus.CONFLICT,
                                "Student has attendance, submissions, or messages and cannot be deleted"));
            }

            // -----------------------------------------
            // Delete student before its login account so foreign-key
            // failures cannot leave an active account behind.
            // -----------------------------------------

            studentRepository.delete(student);

            if (student.getEmail() != null) {

                userRepository
                        .findByEmailIgnoreCase(
                                student.getEmail()
                                        .trim()
                                        .toLowerCase(Locale.ROOT)
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
            logger.error("Failed to delete student", e);
            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(ApiErrorResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "The request could not be completed."));
        }
    }

    private ResponseEntity<RegistrationResponse> registrationAccepted() {
        return ResponseEntity.accepted().body(new RegistrationResponse(
                "If the information can be registered, your request is pending admin review. If you already have an account, sign in."));
    }

    public record RegistrationResponse(String message) {
    }
}
