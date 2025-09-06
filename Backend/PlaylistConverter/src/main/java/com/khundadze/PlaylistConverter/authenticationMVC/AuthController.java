package com.khundadze.PlaylistConverter.authenticationMVC;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController { // returns JWT token in JSON

    private final AuthService service;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        String token = service.register(request);
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        String token = service.login(request);
        return new AuthResponse(token);
    }

}
