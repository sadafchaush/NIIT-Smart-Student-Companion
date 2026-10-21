
package com.niit.studentcompanion.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.UserRepository;
import com.niit.studentcompanion.repository.AssignmentRepository;
import com.niit.studentcompanion.repository.AnnouncementRepository;
import com.niit.studentcompanion.repository.NoteRepository;

@Service
public class FacultyService {

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private NoteRepository noteRepository;


    // =====================================================
    // ADD FACULTY
    //
    // Creates:
    // 1. Faculty record
    // 2. Login User record
    // =====================================================

    @Transactional
    public Faculty addFaculty(Faculty faculty) {

        if (faculty == null) {
            throw new RuntimeException(
                    "Faculty data is required");
        }


        // -----------------------------------------
        // Validate Name
        // -----------------------------------------

        if (faculty.getFullName() == null ||
                faculty.getFullName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Faculty name is required");
        }


        // -----------------------------------------
        // Validate Email
        // -----------------------------------------

        if (faculty.getEmail() == null ||
                faculty.getEmail().trim().isEmpty()) {

            throw new RuntimeException(
                    "Faculty email is required");
        }

        String email = faculty.getEmail()
                .trim()
                .toLowerCase();

        faculty.setEmail(email);


        // -----------------------------------------
        // Validate Password
        // -----------------------------------------

        if (faculty.getPassword() == null ||
                faculty.getPassword().trim().isEmpty()) {

            throw new RuntimeException(
                    "Faculty password is required");
        }

        if (faculty.getPassword().length() < 6) {

            throw new RuntimeException(
                    "Password must contain at least 6 characters");
        }
        String rawPassword = faculty.getPassword();
        faculty.setPassword(passwordEncoder.encode(rawPassword));


        // -----------------------------------------
        // Check Faculty Email
        // -----------------------------------------

        boolean facultyExists =
                facultyRepository.findAll()
                        .stream()
                        .anyMatch(f ->
                                f.getEmail() != null &&
                                f.getEmail()
                                        .equalsIgnoreCase(email)
                        );

        if (facultyExists) {

            throw new RuntimeException(
                    "Faculty email already exists");
        }


        // -----------------------------------------
        // Check User Email
        // -----------------------------------------

        if (userRepository.existsByEmail(email)) {

            throw new RuntimeException(
                    "Email is already registered");
        }


        // -----------------------------------------
        // Save Faculty
        // -----------------------------------------

        Faculty savedFaculty =
                facultyRepository.save(faculty);


        // -----------------------------------------
        // Create Login User
        // -----------------------------------------

        User user = new User();

        user.setName(
                savedFaculty.getFullName()
        );

        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(
                        rawPassword
                )
        );

        user.setRole("FACULTY");


        // -----------------------------------------
        // Save Login User
        // -----------------------------------------

        userRepository.save(user);


        return savedFaculty;
    }


    // =====================================================
    // GET ALL FACULTY
    // =====================================================

    public List<Faculty> getAllFaculty() {

        return facultyRepository.findAll();
    }


    // =====================================================
    // GET FACULTY BY ID
    // =====================================================

    public Faculty getFacultyById(Integer id) {

        return facultyRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Faculty not found with ID: "
                                        + id
                        )
                );
    }


    // =====================================================
    // UPDATE FACULTY
    //
    // Updates:
    // 1. Faculty record
    // 2. Corresponding User login account
    // =====================================================

    @Transactional
    public Faculty updateFaculty(
            Integer id,
            Faculty facultyDetails) {

        Faculty faculty =
                facultyRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found with ID: "
                                                + id
                                )
                        );


        String oldEmail = faculty.getEmail();


        // -----------------------------------------
        // Validate Name
        // -----------------------------------------

        if (facultyDetails.getFullName() == null ||
                facultyDetails.getFullName()
                        .trim()
                        .isEmpty()) {

            throw new RuntimeException(
                    "Faculty name is required");
        }


        // -----------------------------------------
        // Validate Email
        // -----------------------------------------

        if (facultyDetails.getEmail() == null ||
                facultyDetails.getEmail()
                        .trim()
                        .isEmpty()) {

            throw new RuntimeException(
                    "Faculty email is required");
        }

        String newEmail =
                facultyDetails.getEmail()
                        .trim()
                        .toLowerCase();


        // -----------------------------------------
        // Check Changed Email
        // -----------------------------------------

        if (oldEmail == null ||
                !oldEmail.equalsIgnoreCase(newEmail)) {

            if (userRepository.existsByEmail(newEmail)) {

                throw new RuntimeException(
                        "Email is already registered");
            }


            boolean facultyEmailExists =
                    facultyRepository.findAll()
                            .stream()
                            .anyMatch(f ->
                                    f.getFacultyId() != null &&
                                    !f.getFacultyId()
                                            .equals(id) &&
                                    f.getEmail() != null &&
                                    f.getEmail()
                                            .equalsIgnoreCase(
                                                    newEmail
                                            )
                            );

            if (facultyEmailExists) {

                throw new RuntimeException(
                        "Faculty email already exists");
            }
        }


        // -----------------------------------------
        // Update Faculty Information
        // -----------------------------------------

        faculty.setFullName(
                facultyDetails.getFullName()
        );

        faculty.setEmail(newEmail);

        faculty.setPhone(
                facultyDetails.getPhone()
        );

        faculty.setDepartment(
                facultyDetails.getDepartment()
        );

        faculty.setDesignation(
                facultyDetails.getDesignation()
        );


        // -----------------------------------------
        // Update Password
        //
        // Only when a new password is supplied.
        // -----------------------------------------

        String newPassword =
                facultyDetails.getPassword();

        boolean passwordChanged = newPassword != null
                && !newPassword.trim().isEmpty();

        if (newPassword != null &&
                !newPassword.trim().isEmpty()) {

            if (newPassword.length() < 6) {

                throw new RuntimeException(
                        "Password must contain at least 6 characters");
            }

            faculty.setPassword(passwordEncoder.encode(newPassword));
        }


        // -----------------------------------------
        // Save Faculty
        // -----------------------------------------

        Faculty updatedFaculty =
                facultyRepository.save(faculty);


        // -----------------------------------------
        // Find Existing Login User
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
        // Create Login User If Missing
        //
        // Repairs old faculty records that were
        // created before login synchronization.
        // -----------------------------------------

        if (user == null) {

            user = new User();

            user.setName(
                    updatedFaculty.getFullName()
            );

            user.setEmail(newEmail);

            if (updatedFaculty.getPassword() == null
                    || updatedFaculty.getPassword().isBlank()) {

                throw new RuntimeException(
                        "Password is required to create Faculty login account");
            }

            user.setPassword(
                    updatedFaculty.getPassword()
            );

            user.setRole("FACULTY");

        } else {

            // -------------------------------------
            // Update Existing Login User
            // -------------------------------------

            user.setName(
                    updatedFaculty.getFullName()
            );

            user.setEmail(newEmail);

            user.setRole("FACULTY");


            // Change password only when supplied

            if (passwordChanged) {

                user.setPassword(
                        passwordEncoder.encode(
                                newPassword
                        )
                );
            }
        }


        // -----------------------------------------
        // Save Login User
        // -----------------------------------------

        userRepository.save(user);


        return updatedFaculty;
    }


    // =====================================================
    // DELETE FACULTY
    //
    // Deletes:
    // 1. Faculty record
    // 2. Corresponding User login account
    // =====================================================

    @Transactional
    public void deleteFaculty(Integer id) {

        Faculty faculty =
                facultyRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found with ID: "
                                                + id
                                )
                        );

        Long facultyId = id.longValue();
        if (assignmentRepository.existsByFacultyId(facultyId)
                || announcementRepository.existsByFacultyId(facultyId)
                || noteRepository.existsByFacultyId(facultyId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Faculty is still referenced by assignments, announcements, or notes");
        }

        // -----------------------------------------
        // Delete Login User
        // -----------------------------------------

        if (faculty.getEmail() != null) {

            userRepository
                    .findByEmail(
                            faculty.getEmail()
                                    .trim()
                                    .toLowerCase()
                    )
                    .ifPresent(user -> {

                        if ("FACULTY".equalsIgnoreCase(
                                user.getRole())) {

                            userRepository.delete(user);
                        }
                    });
        }


        // -----------------------------------------
        // Delete Faculty
        // -----------------------------------------

        facultyRepository.delete(faculty);
    }
}
