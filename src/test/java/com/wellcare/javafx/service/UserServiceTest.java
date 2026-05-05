package com.wellcare.javafx.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic integration tests for UserService class.
 * Since UserService creates its own DAO instance, these are integration-style tests.
 * Main validation logic is covered by ValidationUtilsTest and data access by UserDAOTest.
 */
@DisplayName("UserService Basic Integration Tests")
class UserServiceTest {

    @Test
    @DisplayName("UserService instantiation should work")
    void testUserServiceCreation() {
        // This test ensures UserService can be instantiated
        // The actual functionality is tested via integration with UserDAOTest
        assertDoesNotThrow(() -> {
            UserService userService = new UserService();
            assertNotNull(userService);
        });
    }

    @Test
    @DisplayName("UserService should have all expected methods")
    void testUserServiceMethodsExist() {
        UserService userService = assertDoesNotThrow(() -> new UserService());

        // Verify that key methods exist (will throw NoSuchMethodException if not)
        assertDoesNotThrow(() -> {
            UserService.class.getMethod("authenticate", String.class, String.class);
            UserService.class.getMethod("register", com.wellcare.javafx.model.User.class);
            UserService.class.getMethod("updateProfile", com.wellcare.javafx.model.User.class);
            UserService.class.getMethod("getUserByUuid", String.class);
            UserService.class.getMethod("getAllUsers");
            UserService.class.getMethod("deleteUser", String.class);
        });
    }

    // Note: Detailed functionality testing is done via:
    // - ValidationUtilsTest for input validation logic
    // - UserDAOTest for data access layer
    // - TestBackend for full integration testing
}