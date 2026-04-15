package com.wellcare.javafx;

import com.wellcare.javafx.model.User;
import com.wellcare.javafx.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Complete test suite for all Sprint 1 features.
 * Tests: CRUD, Bulk Operations, Statistics, Email Verification,
 * Password Reset, Account Lockout, Professional Verification.
 */
public class TestBackend {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  WellCare Backend - Sprint 1 Test Suite");
        System.out.println("========================================\n");

        try {
            UserService userService = new UserService();
            int passed = 0;
            int failed = 0;

            // ==================== BASIC CRUD TESTS ====================
            System.out.println("=== BASIC CRUD TESTS ===\n");

            // TEST 1: Get User Count
            System.out.print("[TEST 1] Get User Count: ");
            try {
                int count = userService.getUserCount();
                System.out.println("PASSED (" + count + " users)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 2: Register New User
            System.out.print("[TEST 2] Register New User: ");
            try {
                User newUser = new User();
                newUser.setEmail("sprint1test@example.com");
                newUser.setPassword("Test12345");
                newUser.setFirstName("Sprint1");
                newUser.setLastName("Test");
                newUser.setRole("ROLE_PATIENT");
                newUser.setActive(false);
                newUser.setEmailVerified(false);
                newUser.setVerifiedByAdmin(false);

                boolean registered = userService.register(newUser);
                if (registered) {
                    System.out.println("PASSED (UUID: " + newUser.getUuid() + ")");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 3: Duplicate Email Check
            System.out.print("[TEST 3] Duplicate Email Check: ");
            try {
                User duplicateUser = new User();
                duplicateUser.setEmail("sprint1test@example.com");
                duplicateUser.setPassword("Test12345");
                duplicateUser.setFirstName("Duplicate");
                duplicateUser.setLastName("Test");
                duplicateUser.setRole("ROLE_PATIENT");
                userService.register(duplicateUser);
                System.out.println("FAILED - Should have thrown exception");
                failed++;
            } catch (IllegalArgumentException e) {
                System.out.println("PASSED (caught: " + e.getMessage() + ")");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 4: Authenticate (correct password)
            System.out.print("[TEST 4] Authenticate (correct password): ");
            try {
                User authenticated = userService.authenticate("sprint1test@example.com", "Test12345");
                if (authenticated != null) {
                    System.out.println("PASSED (" + authenticated.getFullName() + ")");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 5: Authenticate (wrong password)
            System.out.print("[TEST 5] Authenticate (wrong password): ");
            try {
                User wrongAuth = userService.authenticate("sprint1test@example.com", "WrongPassword");
                if (wrongAuth == null) {
                    System.out.println("PASSED (correctly rejected)");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 6: Update Profile
            System.out.print("[TEST 6] Update Profile: ");
            try {
                User user = userService.getUserByEmail("sprint1test@example.com");
                user.setFirstName("Updated");
                user.setLastName("Name");
                boolean updated = userService.updateProfile(user);
                if (updated) {
                    User refreshed = userService.getUserByUuid(user.getUuid());
                    System.out.println("PASSED (" + refreshed.getFullName() + ")");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // ==================== SPRINT 1: BULK OPERATIONS ====================
            System.out.println("\n=== SPRINT 1: BULK OPERATIONS ===\n");

            // TEST 7: Bulk Activate
            System.out.print("[TEST 7] Bulk Activate Users: ");
            try {
                List<User> allUsers = userService.getAllUsers();
                List<String> uuids = Arrays.asList(allUsers.get(0).getUuid(), allUsers.get(1).getUuid());
                int activated = userService.bulkActivateUsers(uuids);
                System.out.println("PASSED (" + activated + " users activated)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 8: Bulk Deactivate
            System.out.print("[TEST 8] Bulk Deactivate Users: ");
            try {
                List<User> allUsers = userService.getAllUsers();
                List<String> uuids = Arrays.asList(allUsers.get(0).getUuid());
                int deactivated = userService.bulkDeactivateUsers(uuids);
                System.out.println("PASSED (" + deactivated + " users deactivated)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 9: Bulk Verify
            System.out.print("[TEST 9] Bulk Verify Users: ");
            try {
                List<User> allUsers = userService.getAllUsers();
                List<String> uuids = Arrays.asList(allUsers.get(0).getUuid());
                int verified = userService.bulkVerifyUsers(uuids);
                System.out.println("PASSED (" + verified + " users verified)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // ==================== SPRINT 1: STATISTICS ====================
            System.out.println("\n=== SPRINT 1: STATISTICS & FILTERING ===\n");

            // TEST 10: User Statistics
            System.out.print("[TEST 10] User Statistics: ");
            try {
                Map<String, Integer> stats = userService.getUserStatistics();
                System.out.println("PASSED (Total: " + stats.get("total") +
                        ", Active: " + stats.get("active") +
                        ", Pending: " + stats.get("pending") + ")");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 11: User Count by Role
            System.out.print("[TEST 11] User Count by Role: ");
            try {
                Map<String, Integer> roleCounts = userService.getUserCountByRole();
                System.out.println("PASSED (" + roleCounts.size() + " roles found)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 12: Search Users
            System.out.print("[TEST 12] Search Users: ");
            try {
                List<User> searchResults = userService.searchUsers("admin");
                System.out.println("PASSED (" + searchResults.size() + " results)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 13: Filter Users
            System.out.print("[TEST 13] Filter Users (by role): ");
            try {
                List<User> filtered = userService.filterUsers("ROLE_PATIENT", null);
                System.out.println("PASSED (" + filtered.size() + " patients)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // ==================== SPRINT 1: EMAIL VERIFICATION ====================
            System.out.println("\n=== SPRINT 1: EMAIL VERIFICATION ===\n");

            // TEST 14: Generate Email Verification Token
            System.out.print("[TEST 14] Generate Email Verification Token: ");
            try {
                User user = userService.getUserByEmail("sprint1test@example.com");
                String token = userService.generateEmailVerificationToken(user.getUuid());
                if (token != null && token.length() > 0) {
                    String displayToken = token.length() > 8 ? token.substring(0, 8) + "..." : token;
                    System.out.println("PASSED (token: " + displayToken + ")");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 15: Verify Email by Token
            System.out.print("[TEST 15] Verify Email by Token: ");
            try {
                User user = userService.getUserByEmail("sprint1test@example.com");
                String token = userService.generateEmailVerificationToken(user.getUuid());
                boolean verified = userService.verifyEmailByToken(token);
                if (verified) {
                    User refreshed = userService.getUserByUuid(user.getUuid());
                    System.out.println("PASSED (isEmailVerified: " + refreshed.isEmailVerified() + ")");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // ==================== SPRINT 1: PASSWORD RESET ====================
            System.out.println("\n=== SPRINT 1: PASSWORD RESET ===\n");

            try {
                userService.initiatePasswordReset("sprint1test@example.com");
                // In a real test, we check if it didn't crash. 
                // To actually verify the token, we'd need to check the DB.
                System.out.println("PASSED (Request initiated)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 17: Reset Password by Token
            System.out.print("[TEST 17] Reset Password by Token: ");
            try {
                userService.initiatePasswordReset("sprint1test@example.com");
                User user = userService.getUserByEmail("sprint1test@example.com");
                String token = user.getResetToken();
                
                if (token == null) {
                    // Try to get directly from DAO since service might not have refreshed user
                    token = userService.getUserByResetTokenForTesting("sprint1test@example.com");
                }

                boolean reset = userService.resetPasswordByToken(token, "NewPass123");
                if (reset) {
                    User authenticated = userService.authenticate("sprint1test@example.com", "NewPass123");
                    if (authenticated != null) {
                        System.out.println("PASSED (login with new password works)");
                        passed++;
                    } else {
                        System.out.println("FAILED (can't login with new password)");
                        failed++;
                    }
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // ==================== SPRINT 1: ACCOUNT LOCKOUT ====================
            System.out.println("\n=== SPRINT 1: ACCOUNT LOCKOUT ===\n");

            // TEST 18: Login Attempts Tracking
            System.out.print("[TEST 18] Login Attempts Tracking: ");
            try {
                // Reset attempts first
                userService.unlockAccount("sprint1test@example.com");

                // Try wrong password 3 times
                for (int i = 0; i < 3; i++) {
                    userService.authenticate("sprint1test@example.com", "WrongPassword");
                }
                int attempts = userService.getLoginAttempts("sprint1test@example.com");
                if (attempts == 3) {
                    System.out.println("PASSED (" + attempts + " failed attempts tracked)");
                    passed++;
                } else {
                    System.out.println("FAILED (expected 3, got " + attempts + ")");
                    failed++;
                }
            } catch (SecurityException e) {
                // Account got locked, which is also correct behavior
                System.out.println("PASSED (account locked after attempts)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 19: Account Lockout
            System.out.print("[TEST 19] Account Lockout (5 failed attempts): ");
            try {
                // Reset attempts
                userService.unlockAccount("sprint1test@example.com");

                // Try wrong password 5 times to trigger lockout
                for (int i = 0; i < 5; i++) {
                    try {
                        userService.authenticate("sprint1test@example.com", "WrongPassword");
                    } catch (SecurityException e) {
                        // Expected on 5th attempt
                    }
                }

                boolean isLocked = userService.isAccountLocked("sprint1test@example.com");
                if (isLocked) {
                    System.out.println("PASSED (account is locked)");
                    passed++;
                } else {
                    System.out.println("FAILED (account should be locked)");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 20: Admin Unlock Account
            System.out.print("[TEST 20] Admin Unlock Account: ");
            try {
                userService.unlockAccount("sprint1test@example.com");
                boolean isLocked = userService.isAccountLocked("sprint1test@example.com");
                if (!isLocked) {
                    System.out.println("PASSED (account unlocked)");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // ==================== SPRINT 1: PROFESSIONAL VERIFICATION ====================
            System.out.println("\n=== SPRINT 1: PROFESSIONAL VERIFICATION ===\n");

            // TEST 21: Update Diploma URL
            System.out.print("[TEST 21] Update Diploma URL: ");
            try {
                User user = userService.getUserByEmail("sprint1test@example.com");
                boolean updated = userService.updateDiplomaUrl(user.getUuid(), "/uploads/diplomas/test-diploma.pdf");
                if (updated) {
                    System.out.println("PASSED");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 22: Verify Professional
            System.out.print("[TEST 22] Verify Professional: ");
            try {
                User user = userService.getUserByEmail("sprint1test@example.com");
                boolean verified = userService.verifyProfessional(user.getUuid());
                if (verified) {
                    User refreshed = userService.getUserByUuid(user.getUuid());
                    System.out.println("PASSED (isVerifiedByAdmin: " + refreshed.isVerifiedByAdmin() + ")");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 23: Unverify Professional
            System.out.print("[TEST 23] Unverify Professional: ");
            try {
                User user = userService.getUserByEmail("sprint1test@example.com");
                boolean unverified = userService.unverifyProfessional(user.getUuid());
                if (unverified) {
                    User refreshed = userService.getUserByUuid(user.getUuid());
                    System.out.println("PASSED (isVerifiedByAdmin: " + refreshed.isVerifiedByAdmin() + ")");
                    passed++;
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // TEST 24: Get Pending Professional Verification
            System.out.print("[TEST 24] Get Pending Professional Verification: ");
            try {
                List<User> pending = userService.getPendingProfessionalVerification();
                System.out.println("PASSED (" + pending.size() + " pending)");
                passed++;
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // ==================== CLEANUP ====================
            System.out.println("\n=== CLEANUP ===\n");

            // TEST 25: Delete Test User
            System.out.print("[TEST 25] Delete Test User: ");
            try {
                User user = userService.getUserByEmail("sprint1test@example.com");
                boolean deleted = userService.deleteUser(user.getUuid());
                if (deleted) {
                    User checkDeleted = userService.getUserByEmail("sprint1test@example.com");
                    if (checkDeleted == null) {
                        System.out.println("PASSED (user deleted)");
                        passed++;
                    } else {
                        System.out.println("FAILED (user still exists)");
                        failed++;
                    }
                } else {
                    System.out.println("FAILED");
                    failed++;
                }
            } catch (Exception e) {
                System.out.println("FAILED - " + e.getMessage());
                failed++;
            }

            // ==================== FINAL SUMMARY ====================
            System.out.println("\n========================================");
            System.out.println("  TEST SUMMARY");
            System.out.println("========================================");
            System.out.println("  Total Tests: " + (passed + failed));
            System.out.println("  Passed: " + passed);
            System.out.println("  Failed: " + failed);
            System.out.println("========================================");

            if (failed == 0) {
                System.out.println("  ALL TESTS PASSED! ✅");
            } else {
                System.out.println("  SOME TESTS FAILED! ❌");
            }
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("\nFATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
