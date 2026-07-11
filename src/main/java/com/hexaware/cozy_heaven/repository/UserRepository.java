package com.hexaware.cozy_heaven.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.cozy_heaven.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);
}
