package com.khundadze.PlaylistConverter.authenticationMVC;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.repo.UserRepository;
import com.khundadze.PlaylistConverter.securityConfig.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Create user
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        // Generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return jwtService.generateToken(userDetails);
    }

    public String login(LoginRequest request) {
        // Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()));

        // Generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        return jwtService.generateToken(userDetails);
    }

    public String oauthLogin(String username) {
        // Ensure user exists in DB; create if first login
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(""); // OAuth user has no password
            userRepository.save(user);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtService.generateToken(userDetails);
    }

}
