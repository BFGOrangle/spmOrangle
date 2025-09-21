package com.spmorangle.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityContextUtil Tests")
class SecurityContextUtilTest {

    private SecurityContext securityContext;
    private JwtAuthenticationToken jwtAuthenticationToken;
    private Jwt jwt;
    private UUID testCognitoSub;
    private String testUsername;
    private String testEmail;
    private String testRole;
    private List<String> testGroups;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        jwtAuthenticationToken = mock(JwtAuthenticationToken.class);
        jwt = mock(Jwt.class);

        testCognitoSub = UUID.randomUUID();
        testUsername = "testuser";
        testEmail = "test@example.com";
        testRole = "USER";
        testGroups = Arrays.asList("user-group", "test-group");
    }

    private void mockSecurityContextWithJwt() {
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
    }

    private void mockSecurityContextWithoutAuth() {
        when(securityContext.getAuthentication()).thenReturn(null);
    }

    private void mockSecurityContextWithNonJwtAuth() {
        Authentication nonJwtAuth = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(nonJwtAuth);
    }

    @Nested
    @DisplayName("getCurrentCognitoSub Tests")
    class GetCurrentCognitoSubTests {

        @Test
        @DisplayName("Should return cognito sub when JWT is present")
        void shouldReturnCognitoSubWhenJwtPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getSubject()).thenReturn(testCognitoSub.toString());
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentCognitoSub();

                // Assert
                assertTrue(result.isPresent());
                assertEquals(testCognitoSub.toString(), result.get());
            }
        }

        @Test
        @DisplayName("Should return empty when no authentication")
        void shouldReturnEmptyWhenNoAuthentication() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentCognitoSub();

                // Assert
                assertFalse(result.isPresent());
            }
        }

        @Test
        @DisplayName("Should return empty when authentication is not JWT")
        void shouldReturnEmptyWhenAuthenticationNotJwt() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithNonJwtAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentCognitoSub();

                // Assert
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("getCurrentCognitoSubUUID Tests")
    class GetCurrentCognitoSubUUIDTests {

        @Test
        @DisplayName("Should return UUID when valid UUID string is present")
        void shouldReturnUUIDWhenValidUUIDStringPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getSubject()).thenReturn(testCognitoSub.toString());
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<UUID> result = SecurityContextUtil.getCurrentCognitoSubUUID();

                // Assert
                assertTrue(result.isPresent());
                assertEquals(testCognitoSub, result.get());
            }
        }

        @Test
        @DisplayName("Should return empty when invalid UUID string")
        void shouldReturnEmptyWhenInvalidUUIDString() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getSubject()).thenReturn("invalid-uuid");
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<UUID> result = SecurityContextUtil.getCurrentCognitoSubUUID();

                // Assert
                assertFalse(result.isPresent());
            }
        }

        @Test
        @DisplayName("Should return empty when no cognito sub available")
        void shouldReturnEmptyWhenNoCognitoSubAvailable() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<UUID> result = SecurityContextUtil.getCurrentCognitoSubUUID();

                // Assert
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("getCurrentUserUsername Tests")
    class GetCurrentUserUsernameTests {

        @Test
        @DisplayName("Should return username when JWT is present")
        void shouldReturnUsernameWhenJwtPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsString("username")).thenReturn(testUsername);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentUserUsername();

                // Assert
                assertTrue(result.isPresent());
                assertEquals(testUsername, result.get());
            }
        }

        @Test
        @DisplayName("Should return empty when no JWT present")
        void shouldReturnEmptyWhenNoJwtPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentUserUsername();

                // Assert
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("getCurrentUserEmail Tests")
    class GetCurrentUserEmailTests {

        @Test
        @DisplayName("Should return email when JWT is present")
        void shouldReturnEmailWhenJwtPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsString("email")).thenReturn(testEmail);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentUserEmail();

                // Assert
                assertTrue(result.isPresent());
                assertEquals(testEmail, result.get());
            }
        }

        @Test
        @DisplayName("Should return empty when no JWT present")
        void shouldReturnEmptyWhenNoJwtPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentUserEmail();

                // Assert
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("getCurrentUserRole Tests")
    class GetCurrentUserRoleTests {

        @Test
        @DisplayName("Should return custom role when present")
        void shouldReturnCustomRoleWhenPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsString("custom:role")).thenReturn(testRole.toLowerCase());
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentUserRole();

                // Assert
                assertTrue(result.isPresent());
                assertEquals(testRole.toUpperCase(), result.get());
            }
        }

        @Test
        @DisplayName("Should return first group when custom role not present")
        void shouldReturnFirstGroupWhenCustomRoleNotPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsString("custom:role")).thenReturn(null);
                when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(testGroups);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentUserRole();

                // Assert
                assertTrue(result.isPresent());
                assertEquals(testGroups.get(0).toUpperCase(), result.get());
            }
        }

        @Test
        @DisplayName("Should return empty when no JWT present")
        void shouldReturnEmptyWhenNoJwtPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<String> result = SecurityContextUtil.getCurrentUserRole();

                // Assert
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("getCurrentCognitoUserSub Tests")
    class GetCurrentCognitoUserSubTests {

        @Test
        @DisplayName("Should return UUID when valid cognito sub present")
        void shouldReturnUUIDWhenValidCognitoSubPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getSubject()).thenReturn(testCognitoSub.toString());
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<UUID> result = SecurityContextUtil.getCurrentCognitoUserSub();

                // Assert
                assertTrue(result.isPresent());
                assertEquals(testCognitoSub, result.get());
            }
        }

        @Test
        @DisplayName("Should return empty when invalid UUID format")
        void shouldReturnEmptyWhenInvalidUUIDFormat() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getSubject()).thenReturn("invalid-uuid");
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<UUID> result = SecurityContextUtil.getCurrentCognitoUserSub();

                // Assert
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("isAuthenticated Tests")
    class IsAuthenticatedTests {

        @Test
        @DisplayName("Should return true when user is authenticated")
        void shouldReturnTrueWhenUserAuthenticated() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                Authentication authentication = mock(Authentication.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.isAuthenticated()).thenReturn(true);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.isAuthenticated();

                // Assert
                assertTrue(result);
            }
        }

        @Test
        @DisplayName("Should return false when user is not authenticated")
        void shouldReturnFalseWhenUserNotAuthenticated() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                Authentication authentication = mock(Authentication.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                when(authentication.isAuthenticated()).thenReturn(false);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.isAuthenticated();

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should return false when authentication is null")
        void shouldReturnFalseWhenAuthenticationNull() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                when(securityContext.getAuthentication()).thenReturn(null);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.isAuthenticated();

                // Assert
                assertFalse(result);
            }
        }
    }

    @Nested
    @DisplayName("hasRole Tests")
    class HasRoleTests {

        @Test
        @DisplayName("Should return true when user has the specified role")
        void shouldReturnTrueWhenUserHasRole() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsString("custom:role")).thenReturn(testRole.toLowerCase());
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.hasRole(testRole);

                // Assert
                assertTrue(result);
            }
        }

        @Test
        @DisplayName("Should return false when user does not have the specified role")
        void shouldReturnFalseWhenUserDoesNotHaveRole() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsString("custom:role")).thenReturn("ADMIN");
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.hasRole("USER");

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should return false when no role available")
        void shouldReturnFalseWhenNoRoleAvailable() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.hasRole("USER");

                // Assert
                assertFalse(result);
            }
        }
    }

    @Nested
    @DisplayName("isInGroup Tests")
    class IsInGroupTests {

        @Test
        @DisplayName("Should return true when user is in the specified group")
        void shouldReturnTrueWhenUserInGroup() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(testGroups);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.isInGroup("user-group");

                // Assert
                assertTrue(result);
            }
        }

        @Test
        @DisplayName("Should return false when user is not in the specified group")
        void shouldReturnFalseWhenUserNotInGroup() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(testGroups);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.isInGroup("admin-group");

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should return false when groups list is null")
        void shouldReturnFalseWhenGroupsListNull() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                when(jwt.getClaimAsStringList("cognito:groups")).thenReturn(null);
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.isInGroup("user-group");

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should return false when no JWT present")
        void shouldReturnFalseWhenNoJwtPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                boolean result = SecurityContextUtil.isInGroup("user-group");

                // Assert
                assertFalse(result);
            }
        }
    }

    @Nested
    @DisplayName("getCurrentJwtToken Tests")
    class GetCurrentJwtTokenTests {

        @Test
        @DisplayName("Should return empty when authentication is not JWT")
        void shouldReturnEmptyWhenAuthenticationNotJwt() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithNonJwtAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<JwtAuthenticationToken> result = SecurityContextUtil.getCurrentJwtToken();

                // Assert
                assertFalse(result.isPresent());
            }
        }

        @Test
        @DisplayName("Should return empty when no authentication")
        void shouldReturnEmptyWhenNoAuthentication() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<JwtAuthenticationToken> result = SecurityContextUtil.getCurrentJwtToken();

                // Assert
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("getCurrentJwt Tests")
    class GetCurrentJwtTests {

        @Test
        @DisplayName("Should return JWT when JWT token is present")
        void shouldReturnJwtWhenJwtTokenPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithJwt();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<Jwt> result = SecurityContextUtil.getCurrentJwt();

                // Assert
                assertTrue(result.isPresent());
                assertEquals(jwt, result.get());
            }
        }

        @Test
        @DisplayName("Should return empty when no JWT token present")
        void shouldReturnEmptyWhenNoJwtTokenPresent() {
            try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
                // Arrange
                mockSecurityContextWithoutAuth();
                mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                // Act
                Optional<Jwt> result = SecurityContextUtil.getCurrentJwt();

                // Assert
                assertFalse(result.isPresent());
            }
        }
    }
}
