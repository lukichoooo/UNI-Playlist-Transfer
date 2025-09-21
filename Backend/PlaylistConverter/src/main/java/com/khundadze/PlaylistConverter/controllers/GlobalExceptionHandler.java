package com.khundadze.PlaylistConverter.controllers;

import com.khundadze.PlaylistConverter.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Helper method to create a consistent error body
    private Map<String, Object> createErrorBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }

    // 401 Unauthorized: User is not logged in at all.
    @ExceptionHandler(UserNotLoggedInException.class)
    public ResponseEntity<Object> handleUserNotLoggedInException(UserNotLoggedInException ex) {
        Map<String, Object> body = createErrorBody(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // 403 Forbidden: User is logged in but lacks permissions for a specific platform.
    @ExceptionHandler({UserNotAuthorizedForStreamingPlatformException.class, TokenNotFoundException.class})
    public ResponseEntity<Object> handleAuthorizationExceptions(RuntimeException ex) {
        Map<String, Object> body = createErrorBody(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // 404 Not Found: A specific resource, like a user, wasn't found.
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, Object> body = createErrorBody(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 400 Bad Request: The client sent invalid data, like an unknown platform name.
    @ExceptionHandler(UnknownStreamingPlatformException.class)
    public ResponseEntity<Object> handleUnknownStreamingPlatformException(UnknownStreamingPlatformException ex) {
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 500 Internal Server Error: For failures during the conversion process.
    @ExceptionHandler(PlaylistConversionException.class)
    public ResponseEntity<Object> handlePlaylistConversionException(PlaylistConversionException ex) {
        Map<String, Object> body = createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Playlist Conversion Error", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 500 Internal Server Error: A fallback for any other unexpected errors.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        // It's crucial to log the full exception here for debugging
        // log.error("Unhandled exception occurred", ex);
        Map<String, Object> body = createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}