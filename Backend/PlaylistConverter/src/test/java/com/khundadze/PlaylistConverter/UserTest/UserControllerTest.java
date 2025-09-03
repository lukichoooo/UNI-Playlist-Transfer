package com.khundadze.PlaylistConverter.UserTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khundadze.PlaylistConverter.controller.UserController;
import com.khundadze.PlaylistConverter.dtos.UserPrivateDto;
import com.khundadze.PlaylistConverter.dtos.UserPublicDto;
import com.khundadze.PlaylistConverter.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService service;

    @InjectMocks
    private UserController controller;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetUserById() throws Exception {
        Long id = 1L;
        UserPrivateDto dto = new UserPrivateDto(id, "testUser", "email@test.com", "pass123");

        when(service.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/users/getUserById/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(service).findById(id);
    }

    @Test
    void testGetUserByUsername() throws Exception {
        String username = "testUser";
        UserPublicDto dto = new UserPublicDto(1L, username);

        when(service.findByUsername(username)).thenReturn(dto);

        mockMvc.perform(get("/api/users/getUserByUsername/{username}", username))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));

        verify(service).findByUsername(username);
    }

    @Test
    void testSearchUsers() throws Exception {
        String partial = "test";
        UserPublicDto dto1 = new UserPublicDto(1L, "test1");
        UserPublicDto dto2 = new UserPublicDto(2L, "test2");
        List<UserPublicDto> dtos = List.of(dto1, dto2);

        when(service.findByUsernameLike(partial)).thenReturn(dtos);

        mockMvc.perform(get("/api/users/searchUsers/{username}", partial))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dtos)));

        verify(service).findByUsernameLike(partial);
    }
}
