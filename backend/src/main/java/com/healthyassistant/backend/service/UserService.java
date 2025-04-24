package com.healthyassistant.backend.service;

import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // This annotation indicates that this class is a service component in the Spring context.
public class UserService {
    @Autowired
    private UserRepository userRepository; //UserRepository实例，用于访问数据库

    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean register(String username, String password){
        if (userRepository.findByUsername(username) != null){
            return false;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);
        return true;
    }
}