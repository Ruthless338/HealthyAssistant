package com.healthyassistant.backend.repository;

import com.healthyassistant.backend.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
// This interface extends JpaRepository to provide CRUD operations for the User
// entity.
// It includes a custom method to find a user by their username.