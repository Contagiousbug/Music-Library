package com.musiclibrary.service;

import com.musiclibrary.dto.UserRegistrationDto;
import com.musiclibrary.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(UserRegistrationDto dto);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    User getCurrentUser();

    User updateProfile(Long userId, String displayName, String email);

    void changePassword(Long userId, String currentPassword, String newPassword);

    void deactivateUser(Long userId);

    List<User> findAllUsers();

    List<User> searchUsers(String query);

    long countActiveUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
