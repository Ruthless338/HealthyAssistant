package com.healthyassistant.backend.controller;

import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        User authenticatedUser = userService.authenticateUser(user.getUsername(), user.getPassword());
        if (authenticatedUser != null) {
            Map<String, Object> response = Map.of(
                "status", 200,
                "user", authenticatedUser,
                "message","Login successful"
            );
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = Map.of(
                "status", 401,
                "user", null,
                "message","Invalid username or password"
            );
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        boolean isRegistered = userService.register(user.getUsername(),user.getPassword());
        if (isRegistered) {
            Map<String, Object> response = Map.of(
                "status", 200,
                "message","Registration successful"
            );
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = Map.of(
                "status", 400,
                "message","Registration failed. Username may already exist."
            );
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody User user) {
        boolean isUpdated = userService.update(user);
        if (isUpdated) {
            Map<String, Object> response = Map.of(
                "status", 200,
                "message","Update successful"
            );
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = Map.of(
                "status", 400,
                "message","Update failed. User not found."
            );
            return ResponseEntity.status(400).body(response);
        }
    }
}