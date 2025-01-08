package com.hexaware.librarymanagement.repository;

import com.hexaware.librarymanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String name);
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndPassword(String email, String password);
    Boolean existsByUsername(String username);
<<<<<<< HEAD
    Optional<User> findById(int id);
=======


>>>>>>> 1186ea151680a4f7b8a2ab06183fa4ee0f36d8cb
}