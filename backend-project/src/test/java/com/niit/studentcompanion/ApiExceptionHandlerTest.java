package com.niit.studentcompanion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.niit.studentcompanion.controller.ApiErrorResponse;
import com.niit.studentcompanion.controller.ApiExceptionHandler;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void constraintFailuresReturnSafeConsistentJsonErrors() {
        var response = handler.handleConstraintViolation(
                new DataIntegrityViolationException("internal database details"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("Conflict", response.getBody().error());
        assertEquals(
                "The request conflicts with existing records or relationships.",
                response.getBody().message());
        assertFalse(response.getBody().message().contains("internal database details"));
    }

    @Test
    void statusExceptionsKeepTheirHttpStatusAndSafeReason() {
        var response = handler.handleResponseStatus(
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        ApiErrorResponse body = response.getBody();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, body.status());
        assertEquals("Unauthorized", body.error());
        assertEquals("Invalid email or password", body.message());
    }
}
