package com.khundadze.PlaylistConverter.services;

import org.springframework.stereotype.Service;

import com.khundadze.PlaylistConverter.dtos.UserPrivateDto;
import com.khundadze.PlaylistConverter.dtos.UserPublicDto;
import com.khundadze.PlaylistConverter.models_db.User;

@Service
public class UserMapper {

    public User toUser(UserPrivateDto userPrivateDto) {
        return User.builder()
                .id(userPrivateDto.id())
                .username(userPrivateDto.username())
                .email(userPrivateDto.email())
                .password(userPrivateDto.password())
                .build();
    }

    public User toUser(UserPublicDto userPrivateDto) {
        return User.builder()
                .id(userPrivateDto.id())
                .username(userPrivateDto.username())
                .build();
    }

    public UserPublicDto toUserPublicDto(User user) {
        return new UserPublicDto(user.getId(), user.getUsername());
    }

    public UserPrivateDto toUserPrivateDto(User user) {
        return new UserPrivateDto(user.getId(), user.getUsername(), user.getEmail(), user.getPassword());
    }
}
