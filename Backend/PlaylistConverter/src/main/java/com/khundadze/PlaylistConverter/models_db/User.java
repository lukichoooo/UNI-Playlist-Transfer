package com.khundadze.PlaylistConverter.models_db;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String email;
    private String password;

    // getters and setters
}
