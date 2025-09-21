package com.spmorangle.common.repository;

import com.spmorangle.common.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByCognitoSub(UUID cognitoSub);

    Optional<User> findByEmail(String email);

    @Modifying
    @Query(value = "UPDATE users SET user_type = ?2 WHERE id = ?1", nativeQuery = true)
    void updateUserTypeById(Long id, String userType);
}