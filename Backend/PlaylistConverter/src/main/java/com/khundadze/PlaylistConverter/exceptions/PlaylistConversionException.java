package com.khundadze.PlaylistConverter.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Failed to process playlist conversion")
public class PlaylistConversionException extends RuntimeException {

    public PlaylistConversionException(String message) {
        super(message);
    }

    public PlaylistConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}