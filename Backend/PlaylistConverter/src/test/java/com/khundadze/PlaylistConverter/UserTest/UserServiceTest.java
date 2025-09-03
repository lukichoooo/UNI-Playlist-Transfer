package com.khundadze.PlaylistConverter.UserTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.khundadze.PlaylistConverter.dtos.UserPrivateDto;
import com.khundadze.PlaylistConverter.dtos.UserPublicDto;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.repo.UserRepository;
import com.khundadze.PlaylistConverter.services.UserService;
import com.khundadze.PlaylistConverter.services.UserMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        UserPrivateDto dto = new UserPrivateDto(1L, "testUser", "email@test.com", "pass123");
        User user = User.builder().id(1L).username("testUser").email("email@test.com").password("pass123").build();
        UserPrivateDto savedDto = new UserPrivateDto(1L, "testUser", "email@test.com", "pass123");

        when(mapper.toUser(dto)).thenReturn(user);
        when(repo.save(user)).thenReturn(user);
        when(mapper.toUserPrivateDto(user)).thenReturn(savedDto);

        UserPrivateDto result = service.save(dto);

        assertEquals(savedDto, result);
        verify(repo).save(user);
        verify(mapper).toUserPrivateDto(user);
    }

    @Test
    void testFindById() {
        Long id = 1L;
        User user = User.builder().id(id).username("testUser").build();
        UserPrivateDto dto = new UserPrivateDto(id, "testUser", null, null);

        when(repo.findById(id)).thenReturn(Optional.of(user));
        when(mapper.toUserPrivateDto(user)).thenReturn(dto);

        UserPrivateDto result = service.findById(id);

        assertEquals(dto, result);
        verify(repo).findById(id);
        verify(mapper).toUserPrivateDto(user);
    }

    @Test
    void testFindByUsername() {
        String username = "testUser";
        User user = User.builder().id(1L).username(username).build();
        UserPublicDto dto = new UserPublicDto(1L, username);

        when(repo.findByUsername(username)).thenReturn(Optional.of(user));
        when(mapper.toUserPublicDto(user)).thenReturn(dto);

        UserPublicDto result = service.findByUsername(username);

        assertEquals(dto, result);
        verify(repo).findByUsername(username);
        verify(mapper).toUserPublicDto(user);
    }

    @Test
    void testFindByUsernameLike() {
        String partial = "test";
        User user1 = User.builder().id(1L).username("test1").build();
        User user2 = User.builder().id(2L).username("test2").build();
        List<User> users = List.of(user1, user2);

        UserPublicDto dto1 = new UserPublicDto(1L, "test1");
        UserPublicDto dto2 = new UserPublicDto(2L, "test2");

        when(repo.findByUsernameLike(partial)).thenReturn(users);
        when(mapper.toUserPublicDto(user1)).thenReturn(dto1);
        when(mapper.toUserPublicDto(user2)).thenReturn(dto2);

        List<UserPublicDto> result = service.findByUsernameLike(partial);

        assertEquals(List.of(dto1, dto2), result);
        verify(repo).findByUsernameLike(partial);
        verify(mapper).toUserPublicDto(user1);
        verify(mapper).toUserPublicDto(user2);
    }
}
