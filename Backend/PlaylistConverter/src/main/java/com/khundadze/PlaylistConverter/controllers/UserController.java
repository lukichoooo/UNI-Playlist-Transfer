package com.khundadze.PlaylistConverter.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khundadze.PlaylistConverter.dtos.UserPrivateDto;
import com.khundadze.PlaylistConverter.dtos.UserPublicDto;
import com.khundadze.PlaylistConverter.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private UserService service;

    @GetMapping("getUserById/{id}")
    public UserPrivateDto getUserById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("getUserByUsername/{username}")
    public UserPublicDto getUserByUsername(@PathVariable String username) {
        return service.findByUsername(username);
    }

    @GetMapping("searchUsers/{username}")
    public List<UserPublicDto> searchUsers(@PathVariable String username) {
        return service.findByUsernameLike(username);
    }

}
