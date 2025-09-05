package com.khundadze.PlaylistConverter.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.dtos.UserPrivateDto;
import com.khundadze.PlaylistConverter.dtos.UserPublicDto;
import com.khundadze.PlaylistConverter.repo.UserRepository;
import com.khundadze.exceotions.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper mapper;
    private final UserRepository repo;

    public UserPrivateDto save(UserPrivateDto userPrivateDto) {
        return mapper.toUserPrivateDto(repo.save(mapper.toUser(userPrivateDto)));
    }

    public UserPrivateDto findById(Long id) {
        return repo.findById(id)
                .map(mapper::toUserPrivateDto)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    public UserPublicDto findByUsername(String username) {
        return repo.findByUsername(username)
                .map(mapper::toUserPublicDto)
                .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));
    }

    public List<UserPublicDto> findByUsernameLike(String username) {
        return repo.findByUsernameLike(username)
                .stream()
                .map(mapper::toUserPublicDto)
                .toList();
    }
}
