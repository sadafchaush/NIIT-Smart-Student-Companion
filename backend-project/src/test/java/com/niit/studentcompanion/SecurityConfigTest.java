package com.niit.studentcompanion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigTest {

    @Test
    void corsAllowsFacultyNotesUploadFromLocalFrontend() {
        CorsConfigurationSource source =
                new SecurityConfig().corsConfigurationSource(
                        "http://localhost:5500,http://127.0.0.1:5500");
        MockHttpServletRequest request =
                new MockHttpServletRequest("OPTIONS", "/notes/upload");
        CorsConfiguration configuration =
                source.getCorsConfiguration(request);

        assertEquals(
                "http://localhost:5500",
                configuration.checkOrigin("http://localhost:5500"));
        assertNull(configuration.checkOrigin("http://localhost:5501"));
        assertTrue(configuration.getAllowedMethods().contains("POST"));
        assertTrue(configuration.getAllowedMethods().contains("OPTIONS"));
        assertTrue(configuration.getAllowCredentials());
    }

    @Test
    void corsRejectsWildcardOriginsWhenCredentialsAreEnabled() {
        SecurityConfig config = new SecurityConfig();
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> config.corsConfigurationSource("*"));
    }

    @Test
    void corsCanBeConfiguredForAnExplicitDeploymentOrigin() {
        CorsConfigurationSource source =
                new SecurityConfig().corsConfigurationSource(
                        "https://companion.example.test");
        CorsConfiguration configuration =
                source.getCorsConfiguration(
                        new MockHttpServletRequest("OPTIONS", "/notes/upload"));

        assertEquals(
                "https://companion.example.test",
                configuration.checkOrigin("https://companion.example.test"));
        assertNull(configuration.checkOrigin("https://untrusted.example.test"));
    }
}
