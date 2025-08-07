package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    
    Page<User> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
        String email, String name, Pageable pageable);
} 