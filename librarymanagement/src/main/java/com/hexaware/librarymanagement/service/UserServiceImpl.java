package com.hexaware.librarymanagement.service;

import com.hexaware.librarymanagement.dto.JWTAuthResponse;
import com.hexaware.librarymanagement.dto.LoginDTO;
import com.hexaware.librarymanagement.dto.RegisterDTO;
import com.hexaware.librarymanagement.dto.UserDTO;
import com.hexaware.librarymanagement.entity.Role;
import com.hexaware.librarymanagement.entity.User;
import com.hexaware.librarymanagement.exception.BadRequestException;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import com.hexaware.librarymanagement.mapper.UserMapper;
import com.hexaware.librarymanagement.repository.RoleRepository;
import com.hexaware.librarymanagement.repository.UserRepository;
import com.hexaware.librarymanagement.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDTO getUserById(int userId) {
        if (userId <= 0) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid User ID",
                    "User ID must be a positive number.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "User Not Found",
                        "User with ID " + userId + " does not exist."));

        // Return the found user as a UserDTO
        return UserMapper.mapToUserDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "No Users Found",
                    "There are no users in the system.");
        }

        // Convert list of User entities to list of UserDTOs
        return users.stream()
                .map(UserMapper::mapToUserDTO)
                .toList();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND,
                        "User Not Found",
                        "No user found with username: " + username));
    }
}

