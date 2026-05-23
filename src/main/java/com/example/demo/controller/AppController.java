package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    @GetMapping("/")
    public Map<String, String> home() {

        logger.info("Home endpoint accessed");

        Map<String, String> response = new HashMap<>();

        response.put("message", "DevOps Demo App Running");
        response.put("timestamp", LocalDateTime.now().toString());

        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {

        logger.info("Health endpoint checked");

        Map<String, String> response = new HashMap<>();

        response.put("status", "UP");

        return response;
    }

    @GetMapping("/logs-test")
    public String logsTest() {

        logger.warn("Warning log generated for ELK testing");
        logger.error("Error log generated for ELK testing");

        return "Logs generated successfully";
    }

    @GetMapping("/validate")
    public Map<String, String> validateInput(@RequestParam String input) {

        logger.info("Validation endpoint accessed");

        Map<String, String> response = new HashMap<>();

        if (input == null || input.trim().isEmpty()) {
            response.put("status", "invalid");
            return response;
        }

        response.put("status", "valid");
        response.put("input", input.trim());

        return response;
    }
}
