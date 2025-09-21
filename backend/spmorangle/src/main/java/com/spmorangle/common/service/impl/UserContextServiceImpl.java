package com.spmorangle.common.service.impl;

import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.common.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserContextServiceImpl implements UserContextService {

    private final UserRepository userRepository;

    public User getRequestingUser() {
        UUID cognitoSub = SecurityContextUtil.getCurrentCognitoSubUUID()
                .orElseThrow(() -> new RuntimeException("Requesting User Cognito sub not available"));
        return userRepository.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> new RuntimeException("Requesting user not found"));
    }

    public boolean isRequestingUserSelfCheckByUserId(Long requestedUserId) {
        return getRequestingUser().getId().equals(requestedUserId);
    }

    public boolean isRequestingUserSelfCheckBySub(UUID cognitoSub) {
        return getRequestingUser().getCognitoSub().equals(cognitoSub);
    }
}
