package com.spmorangle.common.service;

import com.spmorangle.common.model.User;

import java.util.UUID;

public interface UserContextService {

    User getRequestingUser();

    boolean isRequestingUserSelfCheckByUserId(Long userId);

    /**
     * IMPT: This overload users cognito sub not staff ID
     */
    boolean isRequestingUserSelfCheckBySub(UUID cognitoSub);
}
