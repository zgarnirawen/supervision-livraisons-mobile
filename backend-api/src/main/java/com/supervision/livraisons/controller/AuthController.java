package com.supervision.livraisons.controller;

import com.supervision.livraisons.dto.LoginRequest;
import com.supervision.livraisons.dto.LoginResponse;
import com.supervision.livraisons.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Body: { "login": "sami.b", "motPasse": "password123" }
     * Retourne: JWT token + infos utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
