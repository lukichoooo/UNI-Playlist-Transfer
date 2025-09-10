package com.khundadze.PlaylistConverter.exceptions;

public class UserNotAuthorizedForStreamingPlatformException extends RuntimeException {
    public UserNotAuthorizedForStreamingPlatformException(String message) {
        super(message);
    }
}
