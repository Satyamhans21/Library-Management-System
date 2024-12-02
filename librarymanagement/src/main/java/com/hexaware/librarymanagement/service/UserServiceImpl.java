package com.hexaware.librarymanagement.service;

import com.hexaware.librarymanagement.entity.Admin;
import com.hexaware.librarymanagement.entity.User;
import com.hexaware.librarymanagement.repository.AdminRepository;
import com.hexaware.librarymanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public User registerUser(User user) {
        if (user.getEmail() == null || user.getPassword() == null || user.getName() == null) {
            throw new IllegalArgumentException("User name, email, and password cannot be null.");
        }

        // Save the user to the database
        return userRepository.save(user);
    }

    @Override
    public Optional<User> authenticateUser(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public User getUserById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Retrieve all users from the repository
    }
}