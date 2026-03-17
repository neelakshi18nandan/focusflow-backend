package com.focusflow.service;

import com.focusflow.model.Dto.*;
import com.focusflow.model.User;
import com.focusflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Register a new user.
     * Hashes the plain-text password before saving to the database.
     */
    public AuthResponse register(RegisterRequest req) {
        // Check duplicate username
        if (userRepository.existsByUsername(req.getUsername())) {
            return new AuthResponse(null, null, null, "Username already taken", false);
        }
        // Check duplicate email
        if (userRepository.existsByEmail(req.getEmail())) {
            return new AuthResponse(null, null, null, "Email already registered", false);
        }

        // Hash password and save
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        User saved = userRepository.save(user);

        return new AuthResponse(saved.getId(), saved.getUsername(), saved.getEmail(),
                                "Registration successful", true);
    }

    /**
     * Login: check username + password.
     * BCrypt matches hash — plain password is never stored.
     */
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElse(null);

        if (user == null) {
            return new AuthResponse(null, null, null, "User not found", false);
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return new AuthResponse(null, null, null, "Incorrect password", false);
        }

        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(),
                                "Login successful", true);
    }
}
