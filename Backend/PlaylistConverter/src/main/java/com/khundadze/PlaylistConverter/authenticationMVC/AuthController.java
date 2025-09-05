package com.khundadze.PlaylistConverter.authenticationMVC;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService service;

    @GetMapping("register")
    public String register(@RequestBody RegisterRequest request) {
        return service.register(request); // TODO: returns token implement
    }

    @GetMapping("login")
    public String login(@RequestBody LoginRequest request) {
        return service.login(request); // TODO: returns token implement
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication) {
        var userAttributes = authentication.getPrincipal().getAttributes();
        return "Hello " + userAttributes.get("name");
    }

}
