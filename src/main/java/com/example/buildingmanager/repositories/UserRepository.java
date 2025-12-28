package com.example.buildingmanager.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.buildingmanager.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserNameAndStatus(String userName, Integer status);
    Boolean existsByUserName(String userName);
}