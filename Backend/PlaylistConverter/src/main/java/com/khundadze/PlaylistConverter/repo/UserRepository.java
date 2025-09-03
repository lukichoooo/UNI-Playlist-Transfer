package com.khundadze.PlaylistConverter.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khundadze.PlaylistConverter.models_db.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
