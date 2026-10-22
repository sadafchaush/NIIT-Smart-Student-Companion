package com.niit.studentcompanion;

import java.util.List;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import org.springframework.http.ResponseCookie;
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
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5500,http://127.0.0.1:5500}")
            String configuredOrigins) {
        List<String> allowedOrigins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        if (allowedOrigins.isEmpty() || allowedOrigins.contains("*")) {
            throw new IllegalStateException(
                    "CORS must be configured with explicit trusted origins.");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
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
            SecurityContextRepository securityContextRepository,
            Environment environment,
            @Value("${app.cors.allowed-origins:http://localhost:5500,http://127.0.0.1:5500}")
            String configuredOrigins,
            @Value("${server.servlet.session.cookie.secure:false}")
            boolean secureCookies) throws Exception {
        var csrfTokenRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie
                .secure(secureCookies)
                .sameSite("Lax"));

        http
            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource(configuredOrigins)))
            .csrf(csrf -> csrf
                    .csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler(
                            new CsrfTokenRequestAttributeHandler()))
            .securityContext(context ->
                    context.securityContextRepository(securityContextRepository))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(errors -> errors
                    .authenticationEntryPoint((request, response, exception) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(
                                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                    })
                    .accessDeniedHandler((request, response, exception) -> {
                        response.setStatus(403);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(
                                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
                    }))
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

        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            http.addFilterBefore(
                    new HttpsOnlyFilter(),
                    SecurityContextHolderFilter.class);
        }
        http.addFilterBefore(
                new SameSiteCsrfCookieFilter(),
                SecurityContextHolderFilter.class);

        return http.build();
    }

    private static class HttpsOnlyFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            if (request.isSecure()) {
                filterChain.doFilter(request, response);
                return;
            }

            response.setStatus(HttpServletResponse.SC_UPGRADE_REQUIRED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\":426,\"error\":\"Upgrade Required\",\"message\":\"HTTPS is required\"}");
        }
    }

    private static class SameSiteCsrfCookieFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            filterChain.doFilter(request, new HttpServletResponseWrapper(response) {
                @Override
                public void addCookie(jakarta.servlet.http.Cookie cookie) {
                    if (!"XSRF-TOKEN".equals(cookie.getName())) {
                        super.addCookie(cookie);
                        return;
                    }

                    ResponseCookie.ResponseCookieBuilder builder =
                            ResponseCookie.from(cookie.getName(), cookie.getValue())
                                    .path(cookie.getPath() == null ? "/" : cookie.getPath())
                                    .secure(cookie.getSecure())
                                    .httpOnly(cookie.isHttpOnly())
                                    .sameSite("Lax");
                    if (cookie.getDomain() != null) {
                        builder.domain(cookie.getDomain());
                    }
                    if (cookie.getMaxAge() >= 0) {
                        builder.maxAge(Duration.ofSeconds(cookie.getMaxAge()));
                    }
                    super.addHeader("Set-Cookie", builder.build().toString());
                }

                @Override
                public void addHeader(String name, String value) {
                    super.addHeader(name, withSameSite(name, value));
                }

                @Override
                public void setHeader(String name, String value) {
                    super.setHeader(name, withSameSite(name, value));
                }

                private String withSameSite(String name, String value) {
                    if ("Set-Cookie".equalsIgnoreCase(name)
                            && value.startsWith("XSRF-TOKEN=")
                            && !value.toLowerCase(Locale.ROOT).contains("samesite=")) {
                        return value + "; SameSite=Lax";
                    }
                    return value;
                }
            });
        }
    }
}