package com.healthyassistant.backend.controller;

import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController //表明类中方法都是处理http请求
@RequestMapping("/api/register") //表明该类处理请求的根路径
public class RegisterController {
    @Autowired //自动注入UserService
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody User user) {
        boolean isRegistered = userService.register(user.getUsername(),user.getPassword());
        if (isRegistered) {
            return ResponseEntity.ok().body("Registration successful");
        } else {
            return ResponseEntity.status(400).body("Username already exists");
        }
    }
}
