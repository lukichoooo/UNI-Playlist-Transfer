package com.khundadze.PlaylistConverter.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.khundadze.PlaylistConverter.exceptions.UserNotLoggedInException;
import com.khundadze.PlaylistConverter.models_db.User;

@Component
public class CurrentUserProvider { // uses TOKEN FRONTEND SENDS US

    // get id of current user from JWT token frontend sends us
    public Long getId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UserNotLoggedInException();
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }

        throw new UserNotLoggedInException();
    }

    // check if user is logged in / authorized
    public boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && !(auth.getPrincipal() instanceof String && "anonymousUser".equals(auth.getPrincipal()));
    }
}
