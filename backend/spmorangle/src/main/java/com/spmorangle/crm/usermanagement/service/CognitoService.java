package com.spmorangle.crm.usermanagement.service;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CognitoService {

    Optional<AdminCreateUserResponse> createUser(String username, String email,
                                                        String password, boolean isSetAsTemporaryPassword);

    Optional<AdminCreateUserResponse> createUser(String username, String email,
                                                        String password, Map<String, String> userAttributes, boolean isSetAsTemporaryPassword);

    Optional<AdminGetUserResponse> getUser(String username);

    boolean removeUserFromGroup(String usernameOrEmail, String groupName);

    boolean addUserToGroup(String usernameOrEmail, String groupName);

    boolean setUserPassword(String username, String password);

    boolean disableUser(String username);

    boolean enableUser(String username);

    void deleteUser(String usernameOrEmail);

    List<UserType> listUsersInGroup(String groupName);
}