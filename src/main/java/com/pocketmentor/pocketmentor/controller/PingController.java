package com.pocketmentor.pocketmentor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PingController {
    private final JdbcTemplate jdbc;

    @GetMapping("/api/ping")
    public Map<String, String> ping() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return Map.of("status", "ok");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Database unreachable");
        }
    }
}