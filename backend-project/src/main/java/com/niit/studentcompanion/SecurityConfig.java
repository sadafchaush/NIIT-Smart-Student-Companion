package com.niit.studentcompanion;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                    .csrfTokenRepository(
                            CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(
                            new CsrfTokenRequestAttributeHandler()))
            .securityContext(context ->
                    context.securityContextRepository(securityContextRepository))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/students").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/students/email/**")
                    .hasAnyRole("ADMIN", "FACULTY", "STUDENT")
                .requestMatchers(HttpMethod.GET, "/students", "/students/*")
                    .hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers(HttpMethod.GET, "/faculty", "/faculty/*")
                    .hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers(HttpMethod.GET,
                        "/subjects", "/subjects/**",
                        "/syllabus", "/syllabus/**",
                        "/notes", "/notes/**",
                        "/assignments", "/assignments/**",
                        "/announcements", "/announcements/**",
                        "/attendance", "/attendance/**",
                        "/submissions", "/submissions/**",
                        "/question-papers", "/question-papers/**",
                        "/messages", "/messages/**")
                    .hasAnyRole("ADMIN", "FACULTY", "STUDENT")
                .requestMatchers("/students/**", "/faculty/**",
                        "/subjects/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/notes/**",
                        "/assignments/**", "/announcements/**",
                        "/attendance/**", "/question-papers/**")
                    .hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers(HttpMethod.PUT, "/notes/**",
                        "/assignments/**", "/announcements/**",
                        "/attendance/**")
                    .hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers(HttpMethod.DELETE, "/notes/**",
                        "/assignments/**", "/announcements/**",
                        "/attendance/**", "/submissions/**",
                        "/question-papers/**")
                    .hasAnyRole("ADMIN", "FACULTY")
                .requestMatchers(HttpMethod.POST, "/submissions")
                    .hasRole("STUDENT")
                .anyRequest().authenticated()
            );

        return http.build();
    }
}