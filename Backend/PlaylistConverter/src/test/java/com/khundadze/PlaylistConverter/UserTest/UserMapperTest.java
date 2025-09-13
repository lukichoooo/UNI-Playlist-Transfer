package com.khundadze.PlaylistConverter.UserTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.khundadze.PlaylistConverter.dtos.UserPrivateDto;
import com.khundadze.PlaylistConverter.dtos.UserPublicDto;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.services.UserMapper;

public class UserMapperTest {

    UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toUser_fromPrivateDto() {
        UserPrivateDto userPrivateDto = new UserPrivateDto(1L, "username", "password");
        User user = userMapper.toUser(userPrivateDto);
        assert user.getId() == 1L;
        assert user.getUsername().equals("username");
        assert user.getPassword().equals("password");
    }

    @Test
    void toUser_fromPublicDto() {
        UserPublicDto userPublicDto = new UserPublicDto(2L, "publicUser");
        User user = userMapper.toUser(userPublicDto);
        assert user.getId() == 2L;
        assert user.getUsername().equals("publicUser");
        assert user.getPassword() == null;
    }

    @Test
    void toUserPrivateDto() {
        User user = User.builder().id(3L).username("username3").password("password3").build();
        UserPrivateDto userPrivateDto = userMapper.toUserPrivateDto(user);
        assert userPrivateDto.id() == 3L;
        assert userPrivateDto.username().equals("username3");
        assert userPrivateDto.password().equals("password3");
    }

    @Test
    void toUserPublicDto() {
        User user = User.builder().id(4L).username("username4").password("password4").build();
        UserPublicDto userPublicDto = userMapper.toUserPublicDto(user);
        assert userPublicDto.id() == 4L;
        assert userPublicDto.username().equals("username4");
    }
}
