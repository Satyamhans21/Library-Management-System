package com.hexaware.librarymanagement.repository;

import com.hexaware.librarymanagement.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    // Method to check if an admin exists by email
    boolean existsByEmail(String email);

    // Method to find an admin by email
    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByUsername(String username);
}
