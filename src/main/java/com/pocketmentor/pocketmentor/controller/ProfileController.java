package com.pocketmentor.pocketmentor.controller;

import com.pocketmentor.pocketmentor.model.User;
import com.pocketmentor.pocketmentor.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    public record NameRequest(@NotBlank @Size(max = 100) String name) {}
    public record PasswordRequest(@NotBlank String current,
                                  @NotBlank @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String password) {}

    private final UserRepository users;
    private final NoteRepository notes;
    private final QuizAttemptRepository attempts;
    private final PasswordEncoder encoder;

    private User me(Authentication a) {
        return users.findByEmail(a.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    private Map<String, Object> view(User u) {
        return Map.of("name", u.getName(), "email", u.getEmail(), "createdAt", u.getCreatedAt().toString(),
                "notes", notes.findByUserIdOrderByCreatedAtDesc(u.getId()).size(),
                "attempts", attempts.findByUserIdOrderByTakenAtDesc(u.getId()).size());
    }

    @GetMapping
    public Map<String, Object> get(Authentication a) { return view(me(a)); }

    @PutMapping
    public Map<String, Object> rename(@Valid @RequestBody NameRequest r, Authentication a) {
        User u = me(a);
        u.setName(r.name().trim());
        users.save(u);
        return view(u);
    }

    @PutMapping("/password")
    public Map<String, String> password(@Valid @RequestBody PasswordRequest r, Authentication a) {
        User u = me(a);
        if (!encoder.matches(r.current(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        u.setPasswordHash(encoder.encode(r.password()));
        users.save(u);
        return Map.of("status", "ok");
    }
}