package com.niit.studentcompanion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.niit.studentcompanion.entity.Student;
import com.niit.studentcompanion.entity.User;
import com.niit.studentcompanion.entity.Faculty;
import com.niit.studentcompanion.repository.FacultyRepository;
import com.niit.studentcompanion.repository.StudentRepository;
import com.niit.studentcompanion.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthSessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrationLoginAndRoleProtectedSessionWorkWithoutReturningPasswords()
            throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String mobile = String.format(
                Locale.ROOT,
                "%010d",
                Math.abs((long) UUID.randomUUID().getMostSignificantBits())
                        % 10_000_000_000L);
        String email = "test-" + suffix + "@example.invalid";
        String password = "test-password-123";
        User faculty = new User();
        faculty.setName("Integration Faculty");
        faculty.setEmail("faculty-" + suffix + "@example.invalid");
        faculty.setPassword(passwordEncoder.encode("faculty-test-password"));
        faculty.setRole("FACULTY");
        userRepository.save(faculty);

        String registration = objectMapper.writeValueAsString(
                new Registration(
                        "Integration Test",
                        email,
                        password,
                        mobile,
                        "BSc",
                        1));

        Csrf csrf = fetchCsrf();
        MvcResult registered = mockMvc.perform(post("/students")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isAccepted())
                .andReturn();
        String studentResponse = registered.getResponse().getContentAsString();
        if (studentResponse.contains(password) || studentResponse.contains("$2a$")) {
            throw new AssertionError("Registration response exposed a password.");
        }
        Long studentId = studentRepository.findByEmail(email)
                .orElseThrow()
                .getStudentId();

        csrf = fetchCsrf();
        mockMvc.perform(post("/auth/login")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(email, password))))
                .andExpect(status().isUnauthorized());

        MockHttpSession adminSession = createAdminSession();
        csrf = fetchCsrf();
        mockMvc.perform(patch("/students/{id}/approval", studentId)
                        .session(adminSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semester").value(1));

        csrf = fetchCsrf();
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        if (session == null) {
            throw new AssertionError("Login did not establish a server session.");
        }

        mockMvc.perform(get("/students/email/{email}", email).session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/students/email/{email}", "other@example.invalid")
                        .session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
        mockMvc.perform(get("/attendance").session(session))
                .andExpect(status().isForbidden());
        csrf = fetchCsrf();
        mockMvc.perform(post("/assignments")
                        .session(session)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        csrf = fetchCsrf();
        mockMvc.perform(post("/announcements")
                        .session(session)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        csrf = fetchCsrf();
        mockMvc.perform(post("/notes/upload")
                        .session(session)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/subjects").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/syllabus").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/assignments").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/announcements").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/notes").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/question-papers").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/attendance/student/{studentId}", studentId)
                        .session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/submissions/student/{studentId}", studentId)
                        .session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/messages/contacts").session(session))
                .andExpect(status().isOk());
        mockMvc.perform(get("/messages/unread-count").session(session))
                .andExpect(status().isOk());
        Long facultyUserId = faculty.getId();
        mockMvc.perform(get("/messages/{facultyId}", facultyUserId)
                        .session(session))
                .andExpect(status().isOk());
        csrf = fetchCsrf();
        mockMvc.perform(post("/messages")
                        .session(session)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MessageRequest(
                                        facultyUserId,
                                        "Integration message"))))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/students").session(session))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/faculty").session(session))
                .andExpect(status().isForbidden());

        csrf = fetchCsrf();
        mockMvc.perform(patch("/students/{id}/approval", studentId)
                        .session(session)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":2}"))
                .andExpect(status().isForbidden());

        TestTransaction.flagForRollback();
    }

    @Test
    void registrationIgnoresForgedStudentIdAndSemesterAndRequiresAdminApproval()
            throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Student protectedStudent = new Student();
        protectedStudent.setName("Protected Student");
        protectedStudent.setEmail("protected-" + suffix + "@example.invalid");
        protectedStudent.setPassword(passwordEncoder.encode("existing-password"));
        protectedStudent.setMobile(randomMobile());
        protectedStudent.setCourse("BSc");
        protectedStudent.setSemester(2);
        protectedStudent = studentRepository.saveAndFlush(protectedStudent);
        Long protectedId = protectedStudent.getStudentId();

        String email = "forged-" + suffix + "@example.invalid";
        String password = "test-password-123";
        String mobile = randomMobile();
        String payload = objectMapper.writeValueAsString(Map.of(
                "studentId", protectedId,
                "name", "Attacker Registration",
                "email", email,
                "password", password,
                "mobile", mobile,
                "course", "BSc",
                "semester", 6,
                "role", "ADMIN"));

        Csrf csrf = fetchCsrf();
        MvcResult result = mockMvc.perform(post("/students")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andReturn();
        String acceptedRegistration = result.getResponse().getContentAsString();
        csrf = fetchCsrf();
        String duplicatePayload = objectMapper.writeValueAsString(Map.of(
                "studentId", protectedId,
                "name", "Attacker Registration",
                "email", email,
                "password", password,
                "mobile", randomMobile(),
                "course", "BSc",
                "semester", 6,
                "role", "ADMIN"));
        MvcResult duplicate = mockMvc.perform(post("/students")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatePayload))
                .andExpect(status().isAccepted())
                .andReturn();
        assertEquals(
                acceptedRegistration,
                duplicate.getResponse().getContentAsString());
        csrf = fetchCsrf();
        String duplicateMobilePayload = objectMapper.writeValueAsString(Map.of(
                "name", "Another Registration",
                "email", "duplicate-mobile-" + suffix + "@example.invalid",
                "password", password,
                "mobile", mobile,
                "course", "BSc"));
        MvcResult duplicateMobile = mockMvc.perform(post("/students")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateMobilePayload))
                .andExpect(status().isAccepted())
                .andReturn();
        assertEquals(
                acceptedRegistration,
                duplicateMobile.getResponse().getContentAsString());
        Long registeredId = studentRepository.findByEmail(email)
                .orElseThrow()
                .getStudentId();
        assertEquals(
                1,
                studentRepository.findByEmailIn(java.util.List.of(email)).size());

        org.junit.jupiter.api.Assertions.assertNotEquals(protectedId, registeredId);
        Student unchanged = studentRepository.findById(protectedId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Protected Student", unchanged.getName());
        org.junit.jupiter.api.Assertions.assertEquals(2, unchanged.getSemester());
        org.junit.jupiter.api.Assertions.assertNull(
                studentRepository.findById(registeredId).orElseThrow().getSemester());
        org.junit.jupiter.api.Assertions.assertEquals(
                "PENDING_STUDENT",
                userRepository.findByEmail(email).orElseThrow().getRole());

        csrf = fetchCsrf();
        mockMvc.perform(patch("/students/{id}/approval", registeredId)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":6}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        MockHttpSession adminSession = createAdminSession();
        mockMvc.perform(get("/students/pending").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.studentId == " + registeredId + ")]").exists());
        mockMvc.perform(get("/students/{id}", Long.MAX_VALUE)
                        .session(adminSession))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        csrf = fetchCsrf();
        mockMvc.perform(patch("/students/{id}/approval", registeredId)
                        .session(adminSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semester").value(6));
        org.junit.jupiter.api.Assertions.assertEquals(
                6, studentRepository.findById(registeredId).orElseThrow().getSemester());
        org.junit.jupiter.api.Assertions.assertEquals(
                "STUDENT",
                userRepository.findByEmail(email).orElseThrow().getRole());

        csrf = fetchCsrf();
        mockMvc.perform(patch("/students/{id}/approval", registeredId)
                        .session(adminSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        TestTransaction.flagForRollback();
    }

    @Test
    void registrationRequiresStrongPassword() throws Exception {
        Csrf csrf = fetchCsrf();
        mockMvc.perform(post("/students")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Registration(
                                        "Weak Password",
                                        "weak-" + UUID.randomUUID() + "@example.invalid",
                                        "short7",
                                        randomMobile(),
                                        "BSc",
                                        1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        com.niit.studentcompanion.security.PasswordPolicy.MESSAGE));
    }

    @Test
    void loginUsesUsersTableAndDoesNotRevealWhetherEmailExists()
            throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String existingEmail = "login-" + suffix + "@example.invalid";
        String password = "Legacy-password-123";
        User user = new User();
        user.setName("Login Test");
        user.setEmail(existingEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STUDENT");
        userRepository.save(user);

        Student legacyOnlyStudent = new Student();
        legacyOnlyStudent.setName("Legacy Only");
        legacyOnlyStudent.setEmail("legacy-only-" + suffix + "@example.invalid");
        legacyOnlyStudent.setPassword(passwordEncoder.encode(password));
        legacyOnlyStudent.setMobile(randomMobile());
        legacyOnlyStudent.setCourse("BSc");
        studentRepository.save(legacyOnlyStudent);

        Csrf csrf = fetchCsrf();
        String clientAddress = "192.0.2." + Math.floorMod(suffix.hashCode(), 200) + 1;
        MvcResult wrongPassword = performLogin(
                existingEmail, "wrong-password-123", clientAddress, csrf)
                .andExpect(status().isUnauthorized())
                .andReturn();
        MvcResult unknownEmail = performLogin(
                "unknown-" + suffix + "@example.invalid",
                "wrong-password-123",
                clientAddress,
                csrf)
                .andExpect(status().isUnauthorized())
                .andReturn();

        assertEquals(
                wrongPassword.getResponse().getContentAsString(),
                unknownEmail.getResponse().getContentAsString());
        performLogin(
                legacyOnlyStudent.getEmail(), password, clientAddress + "x", csrf)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        for (int attempt = 0; attempt < 8; attempt++) {
            performLogin(
                    "unknown-" + suffix + "@example.invalid",
                    "wrong-password-123",
                    clientAddress,
                    csrf)
                    .andExpect(status().isUnauthorized());
        }
        performLogin(
                existingEmail, "wrong-password-123", clientAddress, csrf)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void adminPasswordUpdateSynchronizesTheUsersPasswordHash()
            throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String email = "update-" + suffix + "@example.invalid";
        String originalPassword = "Original-password-123";
        String mobile = randomMobile();
        Csrf csrf = fetchCsrf();
        mockMvc.perform(post("/students")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Registration(
                                        "Password Update",
                                        email,
                                        originalPassword,
                                        mobile,
                                        "BSc",
                                        1))))
                .andExpect(status().isAccepted());
        Student student = studentRepository.findByEmail(email).orElseThrow();
        String newPassword = "Renewed-password-456";

        csrf = fetchCsrf();
        mockMvc.perform(put("/students/{id}", student.getStudentId())
                        .session(createAdminSession())
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", student.getName(),
                                "email", student.getEmail(),
                                "mobile", student.getMobile(),
                                "course", student.getCourse(),
                                "semester", 1,
                                "password", newPassword))))
                .andExpect(status().isOk());

        assertEquals(
                newPassword,
                userRepository.findByEmail(email)
                        .map(User::getPassword)
                        .map(hash -> passwordEncoder.matches(newPassword, hash)
                                ? newPassword
                                : null)
                        .orElse(null));
        assertTrue(passwordEncoder.matches(
                newPassword,
                studentRepository.findByEmail(email)
                        .orElseThrow()
                        .getPassword()));
    }

    @Test
    void studentCannotCreateUpdateOrDeleteFacultyAccounts() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String studentEmail = "student-" + suffix + "@example.invalid";
        String studentPassword = "student-password-123";
        User studentUser = new User();
        studentUser.setName("Student");
        studentUser.setEmail(studentEmail);
        studentUser.setPassword(passwordEncoder.encode(studentPassword));
        studentUser.setRole("STUDENT");
        userRepository.save(studentUser);

        Faculty faculty = new Faculty();
        faculty.setFullName("Protected Faculty");
        faculty.setEmail("faculty-" + suffix + "@example.invalid");
        faculty.setPassword(passwordEncoder.encode("faculty-password-123"));
        faculty = facultyRepository.saveAndFlush(faculty);
        Integer facultyId = faculty.getFacultyId();

        Csrf csrf = fetchCsrf();
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(studentEmail, studentPassword))))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession studentSession =
                (MockHttpSession) login.getRequest().getSession(false);

        csrf = fetchCsrf();
        mockMvc.perform(post("/faculty")
                        .session(studentSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/faculty/{id}", facultyId)
                        .session(studentSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/faculty/{id}", facultyId)
                        .session(studentSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token()))
                .andExpect(status().isForbidden());

        assertEquals(
                "Protected Faculty",
                facultyRepository.findById(facultyId).orElseThrow().getFullName());
    }

    @Test
    void facultyPasswordsAreStrongAndSynchronizedToUsersTable()
            throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String email = "faculty-" + suffix + "@example.invalid";
        MockHttpSession adminSession = createAdminSession();
        Csrf csrf = fetchCsrf();
        mockMvc.perform(post("/faculty")
                        .session(adminSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Faculty Password Test",
                                "email", email,
                                "password", "short7"))))
                .andExpect(status().isBadRequest());

        csrf = fetchCsrf();
        mockMvc.perform(post("/faculty")
                        .session(adminSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Faculty Password Test",
                                "email", email,
                                "password", "Faculty-password-123",
                                "phone", "5551234567",
                                "department", "Computer Science",
                                "designation", "Lecturer"))))
                .andExpect(status().isOk());

        Faculty faculty = facultyRepository.findByEmailIgnoreCase(email)
                .orElseThrow();
        assertTrue(passwordEncoder.matches(
                "Faculty-password-123",
                userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow()
                        .getPassword()));
        String newPassword = "Renewed-faculty-456";

        csrf = fetchCsrf();
        mockMvc.perform(put("/faculty/{id}", faculty.getFacultyId())
                        .session(adminSession)
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", faculty.getFullName(),
                                "email", email,
                                "password", newPassword,
                                "phone", "5551234567",
                                "department", "Computer Science",
                                "designation", "Lecturer"))))
                .andExpect(status().isOk());

        assertTrue(passwordEncoder.matches(
                newPassword,
                userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow()
                        .getPassword()));
    }

    private MockHttpSession createAdminSession() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String email = "admin-" + suffix + "@example.invalid";
        String password = "admin-test-password";
        User admin = new User();
        admin.setName("Integration Admin");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole("ADMIN");
        userRepository.save(admin);

        Csrf csrf = fetchCsrf();
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .cookie(csrf.cookie())
                        .header("X-XSRF-TOKEN", csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new Credentials(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }

    private org.springframework.test.web.servlet.ResultActions performLogin(
            String email,
            String password,
            String clientAddress,
            Csrf csrf) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .with(request -> {
                    request.setRemoteAddr(clientAddress);
                    return request;
                })
                .cookie(csrf.cookie())
                .header("X-XSRF-TOKEN", csrf.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new Credentials(email, password))));
    }

    private String randomMobile() {
        return String.format(
                Locale.ROOT,
                "%010d",
                Math.abs((long) UUID.randomUUID().getMostSignificantBits())
                        % 10_000_000_000L);
    }

    private Csrf fetchCsrf() throws Exception {
        MvcResult result = mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(
                result.getResponse().getContentAsString());
        return new Csrf(result.getResponse().getCookie("XSRF-TOKEN"),
                body.get("token").asText());
    }

    private record Csrf(jakarta.servlet.http.Cookie cookie, String token) {
    }

    private record Credentials(String email, String password) {
    }

    private record MessageRequest(Long recipientId, String content) {
    }

    private record Registration(
            String name,
            String email,
            String password,
            String mobile,
            String course,
            Integer semester) {
    }
}
