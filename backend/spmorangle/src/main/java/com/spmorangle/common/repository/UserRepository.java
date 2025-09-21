package com.spmorangle.common.repository;

import com.spmorangle.common.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByCognitoSub(UUID cognitoSub);
    Optional<User> findByEmail(String email);
}