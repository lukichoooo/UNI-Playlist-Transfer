package com.khundadze.PlaylistConverter.services;

import com.khundadze.PlaylistConverter.exceptions.UserNotLoggedInException;
import com.khundadze.PlaylistConverter.models_db.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserProvider {

    private Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    public boolean isLoggedIn() {
        return getAuthentication()
                .map(auth ->
                        auth.isAuthenticated() && auth.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .noneMatch(role -> role.equals("ROLE_ANONYMOUS"))
                )
                .orElse(false);
    }


    public Long getRegisteredUserId() {
        return getAuthentication()
                .filter(auth -> isLoggedIn()) // Use our reliable check
                .map(Authentication::getPrincipal)
                .filter(User.class::isInstance)
                .map(User.class::cast)
                .map(User::getId)
                .orElseThrow(UserNotLoggedInException::new);
    }


    public String getGuestId() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .filter(auth -> !isLoggedIn()) // Ensure it's a guest
                .map(Authentication::getName) // .getName() returns the principal's "username", which is the guest UUID
                .orElseThrow(() -> new IllegalStateException("The current user is not a guest."));
    }

    public String getCurrentPrincipalId() {
        return getAuthentication()
                .map(Authentication::getName)
                .orElseThrow(UserNotLoggedInException::new);
    }
}