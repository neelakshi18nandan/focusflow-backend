package com.focusflow.controller;

import com.focusflow.model.Dto.*;
import com.focusflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles user registration and login.
 *
 * POST /api/auth/register  →  create account
 * POST /api/auth/login     →  sign in
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Register a new user.
     * Body: { "username": "neelakshi", "email": "n@email.com", "password": "secret" }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse res = userService.register(req);
        return res.isSuccess()
                ? ResponseEntity.ok(res)
                : ResponseEntity.badRequest().body(res);
    }

    /**
     * Login with username + password.
     * Body: { "username": "neelakshi", "password": "secret" }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse res = userService.login(req);
        return res.isSuccess()
                ? ResponseEntity.ok(res)
                : ResponseEntity.status(401).body(res);
    }
}
