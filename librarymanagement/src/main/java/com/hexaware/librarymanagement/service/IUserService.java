package com.hexaware.librarymanagement.service;

import com.hexaware.librarymanagement.dto.JWTAuthResponse;
import com.hexaware.librarymanagement.dto.LoginDTO;
import com.hexaware.librarymanagement.dto.RegisterDTO;
import com.hexaware.librarymanagement.dto.UserDTO;
import com.hexaware.librarymanagement.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    UserDTO getUserById(int userId); // Return UserDTO for a specific user ID
    List<UserDTO> getAllUsers(); // Return a list of UserDTOs


    User findByUsername(String username);
}
