package com.pocketmentor.pocketmentor.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {
    @GetMapping("/api/hello")
    public String hello(){
        return "Pocket Mentor running";
    }

    @GetMapping("/api/me")
    public String me(org.springframework.security.core.Authentication auth){
        return "Logged in as " + auth.getName();
    }
}
