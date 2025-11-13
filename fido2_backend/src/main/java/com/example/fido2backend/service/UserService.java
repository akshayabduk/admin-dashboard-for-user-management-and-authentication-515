package com.example.fido2backend.service;

import com.example.fido2backend.domain.User;
import com.example.fido2backend.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides user operations.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    // PUBLIC_INTERFACE
    public User upsertUser(String username, String email, String displayName) {
        /** Upsert by username or email to ensure single user record. */
        Optional<User> existing = users.findByUsername(username);
        User u = existing.orElseGet(User::new);
        if (u.getId() == null) u.setId(UUID.randomUUID());
        u.setUsername(username);
        u.setEmail(email);
        u.setDisplayName(displayName);
        u.setActive(true);
        u.setLocked(false);
        return users.save(u);
    }

    // PUBLIC_INTERFACE
    public Optional<User> findByUsernameOrEmail(String id) {
        /** Find user by username or email. */
        return users.findByUsername(id).or(() -> users.findByEmail(id));
    }

    // PUBLIC_INTERFACE
    public List<User> listAll() {
        /** List all users. */
        return users.findAll();
    }

    // PUBLIC_INTERFACE
    public void setActive(UUID userId, boolean active) {
        /** Enable or disable a user. */
        users.findById(userId).ifPresent(u -> {
            u.setActive(active);
            users.save(u);
        });
    }
}
