package com.niit.studentcompanion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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
                .andExpect(status().isCreated())
                .andReturn();
        String studentResponse = registered.getResponse().getContentAsString();
        if (studentResponse.contains(password) || studentResponse.contains("$2a$")) {
            throw new AssertionError("Registration response exposed a password.");
        }
        Long studentId = objectMapper.readTree(studentResponse)
                .get("studentId").asLong();

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
                .andExpect(status().isForbidden());
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
        Long facultyUserId = userRepository.findAllByRoleIgnoreCase("FACULTY")
                .stream()
                .findFirst()
                .orElseThrow()
                .getId();
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

        TestTransaction.flagForRollback();
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
