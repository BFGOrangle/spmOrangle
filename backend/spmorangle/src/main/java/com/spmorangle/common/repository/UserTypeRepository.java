package com.spmorangle.common.repository;

import com.spmorangle.common.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserTypeRepository extends JpaRepository<UserType, Long>, JpaSpecificationExecutor<UserType> {
}