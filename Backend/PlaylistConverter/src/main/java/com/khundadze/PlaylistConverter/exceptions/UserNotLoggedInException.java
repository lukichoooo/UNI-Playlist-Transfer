package com.khundadze.PlaylistConverter.exceptions;

// Custom exception
public class UserNotLoggedInException extends RuntimeException {
    public UserNotLoggedInException() {
        super("No authenticated user found. User must be logged in.");
    }
}