package com.niit.studentcompanion;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("prod")
@org.springframework.test.context.TestPropertySource(
        properties = "CORS_ALLOWED_ORIGINS=https://frontend.example.test")
class ProductionSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void productionRequiresHttpsAndMarksCsrfCookieSecure() throws Exception {
        mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().is(426));

        MvcResult secureResponse = mockMvc.perform(get("/auth/csrf").secure(true))
                .andExpect(status().isOk())
                .andReturn();
        String csrfCookie = secureResponse.getResponse().getHeader("Set-Cookie");

        assertTrue(csrfCookie != null && csrfCookie.contains("Secure"));
        assertTrue(csrfCookie != null && csrfCookie.contains("SameSite=Lax"));
    }
}
