package com.example.library.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", "Library Management System",
                "books", "/api/books",
                "members", "/api/members",
                "loans", "/api/loans");
    }
}