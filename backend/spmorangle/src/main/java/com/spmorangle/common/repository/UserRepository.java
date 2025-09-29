package com.spmorangle.common.repository;

import com.spmorangle.common.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByCognitoSub(UUID cognitoSub);

    Optional<User> findByEmail(String email);

    @Modifying
    @Query(value = "UPDATE users SET user_type = ?2 WHERE id = ?1", nativeQuery = true)
    void updateUserTypeById(Long id, String userType);

    @Query(value = "SELECT u.* FROM syncup.users u JOIN syncup.project_members pm ON u.id = pm.user_id WHERE pm.project_id = :projectId", nativeQuery = true)
    List<User> findProjectMembers(@Param("projectId") Long projectId);

    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findByIdIn(@Param("userIds") List<Long> userIds);
}