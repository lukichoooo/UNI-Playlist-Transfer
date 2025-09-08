package com.khundadze.PlaylistConverter.UserTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import com.khundadze.PlaylistConverter.dtos.UserPrivateDto;
import com.khundadze.PlaylistConverter.dtos.UserPublicDto;
import com.khundadze.PlaylistConverter.exceptions.UserNotFoundException;
import com.khundadze.PlaylistConverter.models_db.User;
import com.khundadze.PlaylistConverter.repo.UserRepository;
import com.khundadze.PlaylistConverter.services.UserMapper;
import com.khundadze.PlaylistConverter.services.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService service;

    @Test
    void testSave() {
        final UserPrivateDto dto = new UserPrivateDto(1L, "testUser", "email@test.com", "pass123");
        final User user = User.builder().id(1L).username("testUser").email("email@test.com").password("pass123")
                .build();
        final UserPrivateDto savedDto = new UserPrivateDto(1L, "testUser", "email@test.com", "pass123");

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
        final Long id = 1L;
        final User user = User.builder().id(id).username("testUser").build();
        final UserPrivateDto dto = new UserPrivateDto(id, "testUser", null, null);

        when(repo.findById(id)).thenReturn(Optional.of(user));
        when(mapper.toUserPrivateDto(user)).thenReturn(dto);

        UserPrivateDto result = service.findById(id);

        assertEquals(dto, result);
        verify(repo).findById(id);
        verify(mapper).toUserPrivateDto(user);
    }

    @Test
    void testFindByIdNotFound() {
        final Long id = 99L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.findById(id));
        verify(repo).findById(id);
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindByUsername() {
        final String username = "testUser";
        final User user = User.builder().id(1L).username(username).build();
        final UserPublicDto dto = new UserPublicDto(1L, username);

        when(repo.findByUsername(username)).thenReturn(Optional.of(user));
        when(mapper.toUserPublicDto(user)).thenReturn(dto);

        UserPublicDto result = service.findByUsername(username);

        assertEquals(dto, result);
        verify(repo).findByUsername(username);
        verify(mapper).toUserPublicDto(user);
    }

    @Test
    void testFindByUsernameNotFound() {
        final String username = "unknown";
        when(repo.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.findByUsername(username));
        verify(repo).findByUsername(username);
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindByUsernameLike() {
        final String partial = "test";
        final User user1 = User.builder().id(1L).username("test1").build();
        final User user2 = User.builder().id(2L).username("test2").build();
        final List<User> users = List.of(user1, user2);

        final UserPublicDto dto1 = new UserPublicDto(1L, "test1");
        final UserPublicDto dto2 = new UserPublicDto(2L, "test2");

        when(repo.findByUsernameLike(partial)).thenReturn(users);
        when(mapper.toUserPublicDto(user1)).thenReturn(dto1);
        when(mapper.toUserPublicDto(user2)).thenReturn(dto2);

        List<UserPublicDto> result = service.findByUsernameLike(partial);

        assertEquals(List.of(dto1, dto2), result);
        verify(repo).findByUsernameLike(partial);
        verify(mapper).toUserPublicDto(user1);
        verify(mapper).toUserPublicDto(user2);
    }

    @Test
    void testLoadUserByUsername() {
        final String username = "testUser";
        final User user = User.builder().id(1L).username(username).password("pass").build();

        when(repo.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername(username);

        assertEquals(user, userDetails);
        verify(repo).findByUsername(username);
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        final String username = "unknown";
        when(repo.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.loadUserByUsername(username));
        verify(repo).findByUsername(username);
    }
}
