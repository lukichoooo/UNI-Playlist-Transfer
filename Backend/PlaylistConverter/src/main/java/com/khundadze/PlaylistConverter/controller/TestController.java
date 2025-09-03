package com.khundadze.PlaylistConverter.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class TestController {

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication) {
        var userAttributes = authentication.getPrincipal().getAttributes();
        return "Hello " + userAttributes.get("name");
    }

}
