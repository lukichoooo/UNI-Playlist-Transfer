package com.khundadze.PlaylistConverter.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.dtos.UserPrivateDto;
import com.khundadze.PlaylistConverter.dtos.UserPublicDto;
import com.khundadze.PlaylistConverter.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private UserMapper mapper;
    private UserRepository repo;

    public UserPrivateDto save(UserPrivateDto userPrivateDto) {
        return mapper.toUserPrivateDto(repo.save(mapper.toUser(userPrivateDto)));
    }

    public UserPrivateDto findById(Long id) {
        return mapper.toUserPrivateDto(repo.findById(id).orElse(null));
    }

    public UserPublicDto findByUsername(String username) {
        return mapper.toUserPublicDto(repo.findByUsername(username).orElse(null));
    }

    public List<UserPublicDto> findByUsernameLike(String username) {
        return repo.findByUsernameLike(username).stream().map(mapper::toUserPublicDto).collect(Collectors.toList());
    }
}
