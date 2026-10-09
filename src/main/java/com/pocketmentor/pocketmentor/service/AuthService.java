package com.pocketmentor.pocketmentor.service;

import com.pocketmentor.pocketmentor.dto.*;
import com.pocketmentor.pocketmentor.model.User;
import com.pocketmentor.pocketmentor.repository.UserRepository;
import com.pocketmentor.pocketmentor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        users.save(u);
        return new AuthResponse(jwt.generate(email), u.getName(), email);
    }

    public AuthResponse login(LoginRequest r) {
        String email = r.email().trim().toLowerCase();
        User u = users.findByEmail(email)
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return new AuthResponse(jwt.generate(email), u.getName(), email);
    }
}