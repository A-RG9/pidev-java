package com.wellcare.javafx.dao;

import com.wellcare.javafx.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database integration tests for UserDAO class.
 * Tests actual database operations with H2 in-memory database for isolation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDAO Database Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String TEST_DB_USER = "sa";
    private static final String TEST_DB_PASSWORD = "";

    private UserDAO userDAO;
    private User testUser;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        // Create H2 in-memory database and tables
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("""
                CREATE TABLE users (
                    uuid VARCHAR(36) PRIMARY KEY,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    birthdate DATE,
                    phone VARCHAR(20),
                    avatar_url VARCHAR(500),
                    address VARCHAR(255),
                    role VARCHAR(50) NOT NULL,
                    license_number VARCHAR(50),
                    specialite VARCHAR(100),
                    diploma_url VARCHAR(500),
                    is_active BOOLEAN DEFAULT FALSE,
                    is_email_verified BOOLEAN DEFAULT FALSE,
                    is_verified_by_admin BOOLEAN DEFAULT FALSE,
                    verification_date TIMESTAMP,
                    email_verification_token VARCHAR(255),
                    email_verification_expires_at TIMESTAMP,
                    reset_token VARCHAR(255),
                    reset_token_expires_at TIMESTAMP,
                    login_attempts INT DEFAULT 0,
                    locked_until TIMESTAMP,
                    last_login_at TIMESTAMP,
                    last_session_id VARCHAR(255),
                    google_id VARCHAR(255),
                    is_two_factor_enabled BOOLEAN DEFAULT FALSE,
                    totp_secret VARCHAR(255),
                    years_of_experience INT DEFAULT 0,
                    about TEXT,
                    education TEXT,
                    certifications TEXT,
                    hospital_affiliations TEXT,
                    awards TEXT,
                    consultation_price INT,
                    lot VARCHAR(100),
                    token VARCHAR(255),
                    rating DECIMAL(3,2) DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            // Create user_sessions table
            stmt.execute("""
                CREATE TABLE user_sessions (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    token VARCHAR(255) UNIQUE NOT NULL,
                    expires_at TIMESTAMP NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                )
                """);

            // Create password_reset_tokens table
            stmt.execute("""
                CREATE TABLE password_reset_tokens (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    token VARCHAR(255) UNIQUE NOT NULL,
                    expires_at TIMESTAMP NOT NULL,
                    used BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                )
                """);

            // Create email_verification_tokens table
            stmt.execute("""
                CREATE TABLE email_verification_tokens (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    token VARCHAR(255) UNIQUE NOT NULL,
                    expires_at TIMESTAMP NOT NULL,
                    used BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
                )
                """);
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Create UserDAO with test database connection
        userDAO = new UserDAO();
        // Override the connection for testing
        try {
            java.lang.reflect.Field connectionField = UserDAO.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            Connection testConnection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
            connectionField.set(userDAO, testConnection);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to inject test database connection", e);
        }

        // Create test user
        testUser = new User();
        testUser.setUuid("test-uuid-123");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$testHashedPassword"); // BCrypt format
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("+1234567890");
        testUser.setRole("ROLE_PATIENT");
        testUser.setLicenseNumber("LIC123456");
        testUser.setActive(true);
        testUser.setEmailVerified(false);
        testUser.setVerifiedByAdmin(false);
        testUser.setCreatedAt(java.time.LocalDateTime.now());
        testUser.setUpdatedAt(java.time.LocalDateTime.now());
        testUser.setEmailVerified(true);
        testUser.setVerifiedByAdmin(false);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up test data
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM user_sessions");
            stmt.execute("DELETE FROM password_reset_tokens");
            stmt.execute("DELETE FROM email_verification_tokens");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Register new user should succeed")
    void testRegister() throws SQLException {
        boolean result = userDAO.register(testUser);

        assertTrue(result);

        // Verify user was inserted
        User retrieved = userDAO.getUserByEmail("test@example.com");
        assertNotNull(retrieved);
        assertNotNull(retrieved.getUuid()); // UUID should be present
        assertEquals("test-uuid-123", retrieved.getUuid()); // Test UUID should be preserved
        assertEquals("John", retrieved.getFirstName());
        assertEquals("Doe", retrieved.getLastName());
    }

    @Test
    @Order(2)
    @DisplayName("Email exists check should work")
    void testEmailExists() throws SQLException {
        // Should return false for non-existent email
        assertFalse(userDAO.emailExists("nonexistent@example.com"));

        // Register user
        userDAO.register(testUser);

        // Should return true for existing email
        assertTrue(userDAO.emailExists("test@example.com"));
    }

    @Test
    @Order(3)
    @DisplayName("Get user by UUID should return correct user")
    void testGetUserByUuid() throws SQLException {
        userDAO.register(testUser);

        User result = userDAO.getUserByUuid("test-uuid-123");

        assertNotNull(result);
        assertEquals("test-uuid-123", result.getUuid());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
    }

    @Test
    @Order(4)
    @DisplayName("Get user by email should return correct user")
    void testGetUserByEmail() throws SQLException {
        userDAO.register(testUser);

        User result = userDAO.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
    }

    @Test
    @Order(5)
    @DisplayName("Get all users should return all registered users")
    void testGetAllUsers() throws SQLException {
        userDAO.register(testUser);

        // Create another user
        User user2 = new User();
        user2.setUuid("test-uuid-456");
        user2.setEmail("test2@example.com");
        user2.setPassword("$2a$10$testHashedPassword2");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setRole("ROLE_DOCTOR");
        user2.setActive(true);
        user2.setEmailVerified(false);
        user2.setVerifiedByAdmin(true);

        userDAO.register(user2);

        List<User> users = userDAO.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("test@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("test2@example.com")));
    }

    @Test
    @Order(6)
    @DisplayName("Get users by role should filter correctly")
    void testGetUsersByRole() throws SQLException {
        userDAO.register(testUser);

        User doctorUser = new User();
        doctorUser.setUuid("doctor-uuid");
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setPassword("$2a$10$doctorPassword");
        doctorUser.setFirstName("Dr");
        doctorUser.setLastName("Smith");
        doctorUser.setRole("ROLE_DOCTOR");
        doctorUser.setActive(true);
        doctorUser.setEmailVerified(true);
        doctorUser.setVerifiedByAdmin(true);

        userDAO.register(doctorUser);

        List<User> patients = userDAO.getUsersByRole("ROLE_PATIENT");
        List<User> doctors = userDAO.getUsersByRole("ROLE_DOCTOR");

        assertEquals(1, patients.size());
        assertEquals("ROLE_PATIENT", patients.get(0).getRole());

        assertEquals(1, doctors.size());
        assertEquals("ROLE_DOCTOR", doctors.get(0).getRole());
    }

    @Test
    @Order(7)
    @DisplayName("Update profile should modify user data")
    void testUpdateProfile() throws SQLException {
        userDAO.register(testUser);

        // Modify user
        testUser.setFirstName("Johnny");
        testUser.setPhone("+0987654321");

        boolean result = userDAO.updateProfile(testUser);
        assertTrue(result);

        // Verify changes
        User updated = userDAO.getUserByUuid("test-uuid-123");
        assertNotNull(updated);
        assertEquals("Johnny", updated.getFirstName());
        assertEquals("+0987654321", updated.getPhone());
    }

    @Test
    @Order(8)
    @DisplayName("Update password should change password hash")
    void testUpdatePassword() throws SQLException {
        userDAO.register(testUser);

        String newPasswordHash = "$2a$10$newPasswordHash";

        boolean result = userDAO.updatePassword("test-uuid-123", newPasswordHash);
        assertTrue(result);

        // Verify password changed
        User updated = userDAO.getUserByUuid("test-uuid-123");
        assertEquals(newPasswordHash, updated.getPassword());
    }

    @Test
    @Order(9)
    @DisplayName("User count should return correct number")
    void testGetUserCount() throws SQLException {
        assertEquals(0, userDAO.getUserCount());

        userDAO.register(testUser);
        assertEquals(1, userDAO.getUserCount());

        User user2 = new User();
        user2.setUuid("uuid2");
        user2.setEmail("user2@example.com");
        user2.setPassword("$2a$10$pass2");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setRole("ROLE_PATIENT");
        user2.setActive(true);
        user2.setEmailVerified(false);
        user2.setVerifiedByAdmin(false);

        userDAO.register(user2);
        assertEquals(2, userDAO.getUserCount());
    }

    @Test
    @Order(10)
    @DisplayName("User count by role should return correct counts")
    void testGetUserCountByRole() throws SQLException {
        userDAO.register(testUser);

        User doctorUser = new User();
        doctorUser.setUuid("doctor-uuid");
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setPassword("$2a$10$doctorPass");
        doctorUser.setFirstName("Dr");
        doctorUser.setLastName("Who");
        doctorUser.setRole("ROLE_DOCTOR");
        doctorUser.setActive(true);
        doctorUser.setEmailVerified(true);
        doctorUser.setVerifiedByAdmin(true);

        userDAO.register(doctorUser);

        java.util.Map<String, Integer> roleCounts = userDAO.getUserCountByRole();

        assertNotNull(roleCounts);
        assertEquals(1, roleCounts.get("ROLE_PATIENT"));
        assertEquals(1, roleCounts.get("ROLE_DOCTOR"));
    }

    @Test
    @Order(11)
    @DisplayName("Search users should find by name")
    void testSearchUsers() throws SQLException {
        userDAO.register(testUser);

        List<User> results = userDAO.searchUsers("John");

        assertEquals(1, results.size());
        assertEquals("John", results.get(0).getFirstName());
    }


    @Test
    @Order(13)
    @DisplayName("Email verification token operations should work")
    void testEmailVerificationTokens() throws SQLException {
        userDAO.register(testUser);

        String token = userDAO.generateEmailVerificationToken("test-uuid-123");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        boolean verified = userDAO.verifyEmailByToken(token);
        assertTrue(verified);
    }

    @Test
    @Order(14)
    @DisplayName("Password reset operations should work")
    void testPasswordReset() throws SQLException {
        userDAO.register(testUser);

        String resetToken = userDAO.generatePasswordResetToken("test@example.com");
        assertNotNull(resetToken);
        assertFalse(resetToken.isEmpty());

        boolean reset = userDAO.resetPasswordByToken(resetToken, "$2a$10$newPassword");
        assertTrue(reset);
    }

    @Test
    @Order(15)
    @DisplayName("Login attempts tracking should work")
    void testLoginAttempts() throws SQLException {
        userDAO.register(testUser);

        // Initially 0 attempts
        assertEquals(0, userDAO.getLoginAttempts("test@example.com"));

        // Increment attempts
        int attempts = userDAO.incrementLoginAttempts("test@example.com");
        assertEquals(1, attempts);

        attempts = userDAO.incrementLoginAttempts("test@example.com");
        assertEquals(2, attempts);

        // Check current attempts
        assertEquals(2, userDAO.getLoginAttempts("test@example.com"));

        // Reset attempts
        userDAO.resetLoginAttempts("test@example.com");
        assertEquals(0, userDAO.getLoginAttempts("test@example.com"));
    }

    @Test
    @Order(16)
    @DisplayName("Account lockout operations should work")
    void testAccountLockout() throws SQLException {
        userDAO.register(testUser);

        // Initially not locked
        assertFalse(userDAO.isAccountLocked("test@example.com"));

        // Lock account
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
        userDAO.lockAccount("test@example.com", lockUntil);

        assertTrue(userDAO.isAccountLocked("test@example.com"));

        // Reset login attempts (which also unlocks)
        userDAO.resetLoginAttempts("test@example.com");
        assertFalse(userDAO.isAccountLocked("test@example.com"));
    }

    @Test
    @Order(17)
    @DisplayName("Bulk operations should work")
    void testBulkOperations() throws SQLException {
        userDAO.register(testUser);

        User user2 = new User();
        user2.setUuid("uuid2");
        user2.setEmail("user2@example.com");
        user2.setPassword("$2a$10$pass2");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setRole("ROLE_PATIENT");
        user2.setActive(false);
        user2.setEmailVerified(false);
        user2.setVerifiedByAdmin(false);

        userDAO.register(user2);

        // Bulk activate
        int activated = userDAO.bulkActivateUsers(List.of("test-uuid-123", "uuid2"));
        assertEquals(2, activated);

        // Bulk deactivate
        int deactivated = userDAO.bulkDeactivateUsers(List.of("test-uuid-123"));
        assertEquals(1, deactivated);

        // Bulk verify
        int verified = userDAO.bulkVerifyUsers(List.of("uuid2"));
        assertEquals(1, verified);
    }

    @Test
    @Order(18)
    @DisplayName("Professional verification operations should work")
    void testProfessionalVerification() throws SQLException {
        userDAO.register(testUser);

        // Update diploma URL
        boolean updated = userDAO.updateDiplomaUrl("test-uuid-123", "http://example.com/diploma.pdf");
        assertTrue(updated);

        // Verify professional
        boolean verified = userDAO.verifyProfessional("test-uuid-123");
        assertTrue(verified);

        // Unverify professional
        boolean unverified = userDAO.unverifyProfessional("test-uuid-123");
        assertTrue(unverified);
    }

    @Test
    @Order(19)
    @DisplayName("Get pending professional verification should work")
    void testGetPendingProfessionalVerification() throws SQLException {
        userDAO.register(testUser);

        User doctorUser = new User();
        doctorUser.setUuid("doctor-uuid");
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setPassword("$2a$10$doctorPass");
        doctorUser.setFirstName("Dr");
        doctorUser.setLastName("Who");
        doctorUser.setRole("ROLE_MEDECIN"); // Use correct role for pending verification
        doctorUser.setActive(true);
        doctorUser.setEmailVerified(true);
        doctorUser.setVerifiedByAdmin(false); // Not verified yet

        userDAO.register(doctorUser);

        List<User> pending = userDAO.getPendingProfessionalVerification();

        // Should include the doctor user who is not verified by admin
        assertTrue(pending.size() >= 1);
    }

    @Test
    @Order(20)
    @DisplayName("Delete user should remove user from database")
    void testDeleteUser() throws SQLException {
        userDAO.register(testUser);

        // Verify user exists
        assertNotNull(userDAO.getUserByUuid("test-uuid-123"));

        // Delete user
        boolean deleted = userDAO.deleteUser("test-uuid-123");
        assertTrue(deleted);

        // Verify user is gone
        assertNull(userDAO.getUserByUuid("test-uuid-123"));
        assertNull(userDAO.getUserByEmail("test@example.com"));
    }

    @Test
    @Order(21)
    @DisplayName("Operations on non-existent users should handle gracefully")
    void testNonExistentUserOperations() throws SQLException {
        // Get non-existent user
        assertNull(userDAO.getUserByUuid("non-existent-uuid"));
        assertNull(userDAO.getUserByEmail("nonexistent@example.com"));

        // Operations on non-existent users should return false
        assertFalse(userDAO.updatePassword("non-existent-uuid", "newpass"));
        assertFalse(userDAO.updateDiplomaUrl("non-existent-uuid", "url"));
        assertFalse(userDAO.verifyProfessional("non-existent-uuid"));
        assertFalse(userDAO.unverifyProfessional("non-existent-uuid"));
        assertFalse(userDAO.deleteUser("non-existent-uuid"));

        // Login attempts for non-existent users
        assertEquals(0, userDAO.getLoginAttempts("nonexistent@example.com"));
        assertFalse(userDAO.isAccountLocked("nonexistent@example.com"));
    }
}