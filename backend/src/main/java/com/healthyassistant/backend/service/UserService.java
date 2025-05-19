package com.healthyassistant.backend.service;

import com.healthyassistant.backend.model.User;
import com.healthyassistant.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // This annotation indicates that this class is a service component in the
         // Spring context.
public class UserService {
    @Autowired
    private UserRepository userRepository; // UserRepository实例，用于访问数据库

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean register(String username, String password) {
        System.out.println("Register finding: " + userRepository.findByUsername(username));
        if (userRepository.findByUsername(username).isPresent()) {
            return false;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean update(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 检测字段变更
        boolean modified = !Objects.equals(existingUser.getAge(), user.getAge()) ||
                !Objects.equals(existingUser.getHeight(), user.getHeight()) ||
                !Objects.equals(existingUser.getWeight(), user.getWeight()) ||
                !Objects.equals(existingUser.getGoal(), user.getGoal()) ||
                !Objects.equals(existingUser.getInterest(), user.getInterest()) ||
                !Objects.equals(existingUser.getPart(), user.getPart()) ||
                !Objects.equals(existingUser.getGender(), user.getGender());

        if (modified) {
            existingUser.setProfileModified(true); // 标记需要重新生成计划
        }

        existingUser.setAge(user.getAge());
        existingUser.setHeight(user.getHeight());
        existingUser.setWeight(user.getWeight());
        existingUser.setGoal(user.getGoal());
        existingUser.setInterest(user.getInterest());
        existingUser.setPart(user.getPart());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setGender(user.getGender());
        userRepository.save(existingUser);
        return true;
    }

    public Optional<User> getUserById(Long userId) {
        System.out.println("Fetching user with ID: " + userId);
        System.out.println("User: " + userRepository.findById(userId));
        return userRepository.findById(userId);
    }
}