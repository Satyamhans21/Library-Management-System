package com.hexaware.librarymanagement.service;

// Import necessary packages and classes

import com.hexaware.librarymanagement.dto.*;
import com.hexaware.librarymanagement.entity.Admin;
import com.hexaware.librarymanagement.entity.Role;
import com.hexaware.librarymanagement.entity.User;
import com.hexaware.librarymanagement.exception.BadRequestException;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import com.hexaware.librarymanagement.repository.AdminRepository;
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

import java.util.HashSet;
import java.util.Set;

/**
 * AuthServiceImpl provides authentication and registration functionalities
 * for Users and Admins. It implements the IAuthService interface.
 */
@Service
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AdminRepository adminRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;

    // Constructor injection for dependencies
    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository, RoleRepository roleRepository, AdminRepository adminRepository, PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticates a user or admin based on the provided login credentials.
     *
     * @param dto Login details (username and password).
     * @return JWTAuthResponse containing the JWT token and user/admin details.
     */
    @Override
    public JWTAuthResponse authenticateUser(LoginDTO dto) {
        // Authenticate the credentials (common for both User and Admin)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        // Set authentication context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        // Try finding the user by username
        User user = userRepository.findByUsername(dto.getUsername()).orElse(null);
        if (user != null) {
            // Map user details to UserDTO
            UserDTO userDto = mapToUserDTO(user);

            // Return the JWTAuthResponse for the user
            return new JWTAuthResponse(token, userDto, null);
        }

        // If not a user, try finding the admin by username
        Admin admin = adminRepository.findByUsername(dto.getUsername()).orElseThrow(() ->
                new CRUDAPIException(HttpStatus.UNAUTHORIZED, "Authentication Failed",
                        "No user or admin found with the provided username."));

        // Map admin details to AdminDTO
        AdminDTO adminDto = mapToAdminDTO(admin);

        // Return the JWTAuthResponse for the admin
        return new JWTAuthResponse(token, null, adminDto);
    }

    // Helper method for mapping User entity to UserDTO
    private UserDTO mapToUserDTO(User user) {
        UserDTO userDto = new UserDTO();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setUsername(user.getUsername());
        userDto.setGender(user.getGender());
        userDto.setContactNumber(user.getContactNumber());
        userDto.setAddress(user.getAddress());
        userDto.setProfilePicture(user.getProfilePicture());

        // Assign the highest role
        String role = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("ROLE_USER");
        userDto.setRole(role);

        return userDto;
    }

    // Helper method for mapping Admin entity to AdminDTO
    private AdminDTO mapToAdminDTO(Admin admin) {
        AdminDTO adminDto = new AdminDTO();
        adminDto.setAdminId(admin.getAdminId());
        adminDto.setAdminName(admin.getAdminName());
        adminDto.setUsername(admin.getUsername());
        adminDto.setEmail(admin.getEmail());
        adminDto.setGender(admin.getGender());
        adminDto.setContactNumber(admin.getContactNumber());
        adminDto.setAddress(admin.getAddress());
        adminDto.setProfilePicture(admin.getProfilePicture());
        return adminDto;
    }

    /**
     * Registers a new user in the system.
     *
     * @param dto Registration details of the user.
     * @return Success message if registration is successful.
     */
    @Override
    @Transactional
    public String register(RegisterDTO dto) {
        // Check if username already exists
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Username already exists.");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        // Map RegisterDTO to User entity
        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Encode password
        user.setGender(dto.getGender());

        // Set default contact number if not provided
        user.setContactNumber(dto.getContactNumber() != null && !dto.getContactNumber().isEmpty() ? dto.getContactNumber() : "Unknown");

        // Set default address if not provided
        user.setAddress(dto.getAddress() != null && !dto.getAddress().isEmpty() ? dto.getAddress() : "Unknown");

        user.setProfilePicture(dto.getProfilePicture());

        // Handle roles
        Set<Role> roles = new HashSet<>();
        if (dto.getRole() != null && !dto.getRole().isEmpty()) {
            // Attempt to assign the role provided in the DTO
            Role providedRole = roleRepository.findByName(dto.getRole())
                    .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "Invalid role: " + dto.getRole()));
            roles.add(providedRole);
        } else {
            // Assign default role if none provided
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        // Create the default role if it doesn't exist
                        Role newRole = new Role();
                        newRole.setName("ROLE_USER");
                        return roleRepository.save(newRole); // Save the newly created role
                    });
            roles.add(defaultRole);
        }
        user.setRoles(roles);

        // Save the user to the database
        userRepository.save(user);

        return "Registration Successful!";
    }

    /**
     * Registers a new admin in the system.
     *
     * @param adminDTO Registration details of the admin.
     * @return Success message if registration is successful.
     */
    @Override
    public String registerAdmin(AdminDTO adminDTO) {
        if (adminRepository.existsByEmail(adminDTO.getEmail())) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        // Map AdminDTO to Admin entity
        Admin admin = new Admin();
        admin.setUsername(adminDTO.getUsername()); // Use the username field for login
        admin.setAdminName(adminDTO.getAdminName()); // Retain adminName for display purposes (not used for login)
        admin.setEmail(adminDTO.getEmail());
        admin.setPassword(passwordEncoder.encode(adminDTO.getPassword())); // Encode password
        admin.setGender(adminDTO.getGender());

        // Set default contact number if not provided
        admin.setContactNumber(
                adminDTO.getContactNumber() != null && !adminDTO.getContactNumber().isEmpty()
                        ? adminDTO.getContactNumber()
                        : "Unknown"
        );

        // Set default address if not provided
        admin.setAddress(
                adminDTO.getAddress() != null && !adminDTO.getAddress().isEmpty()
                        ? adminDTO.getAddress()
                        : "Unknown"
        );

        admin.setProfilePicture(adminDTO.getProfilePicture());

        // Save the admin to the database
        adminRepository.save(admin);

        return "Admin Registration Successful!";
    }
}
