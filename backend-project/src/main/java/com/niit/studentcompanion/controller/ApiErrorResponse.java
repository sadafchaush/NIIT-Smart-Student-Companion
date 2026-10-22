package com.niit.studentcompanion.controller;

import org.springframework.http.HttpStatus;

public record ApiErrorResponse(int status, String error, String message) {

    public static ApiErrorResponse of(HttpStatus status, String message) {
        return new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message);
    }
}
