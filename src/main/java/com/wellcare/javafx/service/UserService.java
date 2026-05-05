package com.wellcare.javafx.service;

import com.wellcare.javafx.dao.UserDAO;
import com.wellcare.javafx.model.User;
import com.wellcare.javafx.util.ValidationUtils;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Business logic layer for User operations.
 * Provides validation, password hashing, and orchestrates DAO calls.
 * Includes all Sprint 1 features: bulk operations, email verification,
 * password reset, account lockout, and professional verification.
 */
public class UserService {
    private final UserDAO userDAO;
    private final EmailService emailService;
    private final DiplomaVerificationService diplomaVerificationService;
    
    // Account lockout settings
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    /**
     * Constructor that initializes the UserDAO.
     * @throws SQLException if DAO initialization fails
     */
    public UserService() throws SQLException {
        this.userDAO = new UserDAO();
        this.emailService = new EmailService();
        this.diplomaVerificationService = new DiplomaVerificationService();
    }

    /**
     * Authenticates a user with email and plain text password.
     * Includes account lockout logic after failed attempts.
     * @param email the user's email
     * @param plainPassword the plain text password
     * @return the authenticated User or null
     * @throws SQLException if authentication fails
     */
    public User authenticate(String email, String plainPassword) throws SQLException {
        // Validate login fields
        String loginError = ValidationUtils.validateLogin(email, plainPassword);
        if (loginError != null) {
            throw new IllegalArgumentException(loginError);
        }
        
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Aucun compte n'existe avec cette adresse email");
        }

        // Check if account is locked
        if (userDAO.isAccountLocked(email)) {
            throw new SecurityException("Votre compte a été temporairement verrouillé après " +
                MAX_LOGIN_ATTEMPTS + " tentatives échouées. Veuillez réessayer dans " +
                LOCKOUT_MINUTES + " minute(s)");
        }

        // Use BCrypt library compatible with PHP password_verify() - supports $2y$ format natively
        if (BCrypt.verifyer().verify(plainPassword.toCharArray(), user.getPassword()).verified) {
            // Reset login attempts on successful login
            userDAO.resetLoginAttempts(email);
            return user;
        }

        // Increment failed login attempts
        int attempts = userDAO.incrementLoginAttempts(email);
        int remaining = MAX_LOGIN_ATTEMPTS - attempts;

        if (remaining <= 0) {
            // Lock account for 30 minutes
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
            userDAO.lockAccount(email, lockUntil);
            throw new SecurityException("Votre compte a été temporairement verrouillé après " +
                MAX_LOGIN_ATTEMPTS + " tentatives échouées. Veuillez réessayer dans " +
                LOCKOUT_MINUTES + " minute(s)");
        }

        // Return null for wrong password (don't throw exception)
        return null;
    }

    /**
     * Registers a new user after validation.
     * @param user the User to register
     * @return true if registration was successful
     * @throws SQLException if registration fails
     * @throws IllegalArgumentException if validation fails
     */
    public boolean register(User user) throws SQLException {
        // Validate using ValidationUtils
        String validationError = ValidationUtils.validateRegistration(
            user.getEmail(),
            user.getPassword(),
            user.getPassword(), // confirmPassword same as password in service layer
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
            user.getLicenseNumber(),
            user.getRole()
        );
        
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        
        // Check email uniqueness
        if (userDAO.emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée");
        }
        
        // Check phone uniqueness (if provided)
        // Note: Add phoneExists method to DAO if needed
        
        // Hash password using BCrypt
        user.setPassword(hashPassword(user.getPassword()));
        
        return userDAO.register(user);
    }

    /**
     * Updates a user's password.
     * @param uuid the user's UUID
     * @param oldPassword the current password for verification
     * @param newPassword the new password
     * @return true if password was updated
     * @throws SQLException if update fails
     * @throws IllegalArgumentException if old password is incorrect
     */
    public boolean updatePassword(String uuid, String oldPassword, String newPassword) throws SQLException {
        User user = userDAO.getUserByUuid(uuid);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        if (!BCrypt.verifyer().verify(oldPassword.toCharArray(), user.getPassword()).verified) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        if (!ValidationUtils.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères avec une majuscule, une minuscule, un chiffre et un caractère spécial");
        }
        
        return userDAO.updatePassword(uuid, hashPassword(newPassword));
    }

    /**
     * Retrieves all users.
     * @return a list of all users
     * @throws SQLException if query fails
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    /**
     * Retrieves a user by UUID.
     * @param uuid the user's UUID
     * @return the User or null
     * @throws SQLException if query fails
     */
    public User getUserByUuid(String uuid) throws SQLException {
        return userDAO.getUserByUuid(uuid);
    }

    /**
     * Retrieves a user by email.
     * @param email the user's email
     * @return the User or null
     * @throws SQLException if query fails
     */
    public User getUserByEmail(String email) throws SQLException {
        return userDAO.getUserByEmail(email);
    }

    /**
     * Retrieves users by role.
     * @param role the user role
     * @return a list of users
     * @throws SQLException if query fails
     */
    public List<User> getUsersByRole(String role) throws SQLException {
        return userDAO.getUsersByRole(role);
    }

    /**
     * Deletes a user.
     * @param uuid the user's UUID
     * @return true if deletion was successful
     * @throws SQLException if deletion fails
     */
    public boolean deleteUser(String uuid) throws SQLException {
        return userDAO.deleteUser(uuid);
    }

    /**
     * Activates a user account.
     * @param uuid the user's UUID
     * @return true if activation was successful
     * @throws SQLException if activation fails
     */
    public boolean activateUser(String uuid) throws SQLException {
        return userDAO.activateUser(uuid);
    }

    /**
     * Deactivates a user account.
     * @param uuid the user's UUID
     * @return true if deactivation was successful
     * @throws SQLException if deactivation fails
     */
    public boolean deactivateUser(String uuid) throws SQLException {
        return userDAO.deactivateUser(uuid);
    }

    /**
     * Gets the total user count.
     * @return the number of users
     * @throws SQLException if query fails
     */
    public int getUserCount() throws SQLException {
        return userDAO.getUserCount();
    }

    // ==================== SPRINT 1: ADMIN BULK OPERATIONS ====================

    /**
     * Bulk activates multiple users.
     * @param uuids list of user UUIDs to activate
     * @return number of users activated
     * @throws SQLException if operation fails
     */
    public int bulkActivateUsers(List<String> uuids) throws SQLException {
        return userDAO.bulkActivateUsers(uuids);
    }

    /**
     * Bulk deactivates multiple users.
     * @param uuids list of user UUIDs to deactivate
     * @return number of users deactivated
     * @throws SQLException if operation fails
     */
    public int bulkDeactivateUsers(List<String> uuids) throws SQLException {
        return userDAO.bulkDeactivateUsers(uuids);
    }

    /**
     * Bulk verifies multiple users (admin verification).
     * @param uuids list of user UUIDs to verify
     * @return number of users verified
     * @throws SQLException if operation fails
     */
    public int bulkVerifyUsers(List<String> uuids) throws SQLException {
        return userDAO.bulkVerifyUsers(uuids);
    }

    /**
     * Bulk deletes multiple users.
     * @param uuids list of user UUIDs to delete
     * @return number of users deleted
     * @throws SQLException if operation fails
     */
    public int bulkDeleteUsers(List<String> uuids) throws SQLException {
        return userDAO.bulkDeleteUsers(uuids);
    }

    // ==================== SPRINT 1: USER STATISTICS ====================

    /**
     * Gets user count by role.
     * @return map of role -> count
     * @throws SQLException if query fails
     */
    public Map<String, Integer> getUserCountByRole() throws SQLException {
        return userDAO.getUserCountByRole();
    }

    /**
     * Gets user statistics summary.
     * @return map with total, active, inactive, verified, pending counts
     * @throws SQLException if query fails
     */
    public Map<String, Integer> getUserStatistics() throws SQLException {
        return userDAO.getUserStatistics();
    }

    // ==================== SPRINT 1: PENDING VERIFICATION ====================

    /**
     * Gets users pending admin verification.
     * @return list of users not yet verified by admin
     * @throws SQLException if query fails
     */
    public List<User> getPendingVerificationUsers() throws SQLException {
        return userDAO.getPendingVerificationUsers();
    }

    // ==================== SPRINT 1: USER FILTERING ====================

    /**
     * Searches users by keyword (name or email).
     * @param keyword search term
     * @return list of matching users
     * @throws SQLException if query fails
     */
    public List<User> searchUsers(String keyword) throws SQLException {
        return userDAO.searchUsers(keyword);
    }

    /**
     * Filters users by role and status.
     * @param role user role (null for all)
     * @param isActive active status (null for all)
     * @return list of filtered users
     * @throws SQLException if query fails
     */
    public List<User> filterUsers(String role, Boolean isActive) throws SQLException {
        return userDAO.filterUsers(role, isActive);
    }

    // ==================== SPRINT 1: EMAIL VERIFICATION ====================

    /**
     * Generates email verification token for a user.
     * @param uuid user UUID
     * @return the generated token
     * @throws SQLException if operation fails
     */
    public String generateEmailVerificationToken(String uuid) throws SQLException {
        return userDAO.generateEmailVerificationToken(uuid);
    }

    /**
     * Verifies email using token.
     * @param token verification token
     * @return true if verification successful
     * @throws SQLException if operation fails
     */
    public boolean verifyEmailByToken(String token) throws SQLException {
        return userDAO.verifyEmailByToken(token);
    }

    /**
     * Gets user by email verification token.
     * @param token verification token
     * @return the User or null
     * @throws SQLException if query fails
     */
    public User getUserByVerificationToken(String token) throws SQLException {
        return userDAO.getUserByVerificationToken(token);
    }

    // ==================== SPRINT 1: PASSWORD RESET ====================
    
    // In-memory strictly concurrent rate limiters for "3 resets per hour" security constraint
    private static final java.util.Map<String, Integer> resetRequests = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, Long> lastResetTime = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Initiates password reset by generating a token and sending an email.
     * Implements strict rate-limiting (max 3/hr) and prevents user enumeration.
     * @param email user email
     * @throws SQLException if operation fails
     * @throws IllegalStateException if rate limit exceeded
     */
    public void initiatePasswordReset(String email) throws SQLException, IllegalStateException {
        long now = System.currentTimeMillis();
        
        // Clear rate limit if > 1 hour has passed
        if (lastResetTime.containsKey(email) && (now - lastResetTime.get(email)) > 3600000) {
            resetRequests.remove(email);
        }

        int attempts = resetRequests.getOrDefault(email, 0);
        if (attempts >= 3) {
            throw new IllegalStateException("Limite maximale de 3 demandes de réinitialisation par heure dépassée.");
        }

        // Apply hit
        resetRequests.put(email, attempts + 1);
        lastResetTime.put(email, now);

        String token = userDAO.generatePasswordResetToken(email);
        
        // If user exists, token is generated and we secretly dispatch the email.
        if (token != null) {
            new Thread(() -> {
                new EmailService().sendPasswordResetEmail(email, token);
            }).start();
        }
        
        // Return silently. If token == null (user doesn't exist), we still succeed to stop enumeration attacks.
    }

    /**
     * Resets password using token.
     * @param token reset token
     * @param newPassword new plain text password
     * @return true if reset successful
     * @throws SQLException if operation fails
     */
    public boolean resetPasswordByToken(String token, String newPassword) throws SQLException {
        if (!ValidationUtils.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères avec une majuscule, une minuscule, un chiffre et un caractère spécial");
        }
        
        User user = userDAO.getUserByResetToken(token);
        boolean success = userDAO.resetPasswordByToken(token, hashPassword(newPassword));
        
        if (success && user != null) {
            // Dispatch confirmation email
            new Thread(() -> {
                new EmailService().sendPasswordChangedConfirmation(user.getEmail());
            }).start();
        }
        
        return success;
    }

    /**
     * Updates user profile information.
     * @param user the user to update
     * @return true if update was successful
     * @throws SQLException if operation fails
     */
    public boolean updateProfile(User user) throws SQLException {
        // Validation could be added here
        if (user == null || user.getUuid() == null) {
            return false;
        }
        
        return userDAO.updateUserProfile(user);
    }

    /**
     * Updates user as an administrator.
     * @param user the user to update
     * @return true if update successful
     * @throws SQLException if operation fails
     */
    public boolean adminUpdateUser(User user) throws SQLException {
        if (user == null || user.getUuid() == null) return false;
        return userDAO.adminUpdateUser(user);
    }

    /**
     * Resets a user's password as an administrator.
     * Generates a new temporary password and sends it via email.
     * @param uuid user UUID
     * @return the new temporary password
     * @throws SQLException if operation fails
     */
    public String adminResetPassword(String uuid) throws SQLException {
        User user = userDAO.getUserByUuid(uuid);
        if (user == null) throw new IllegalArgumentException("User not found");

        String tempPass = java.util.UUID.randomUUID().toString().substring(0, 8);
        String hashedPass = hashPassword(tempPass);
        
        if (userDAO.updatePassword(uuid, hashedPass)) {
            new Thread(() -> {
                emailService.sendPasswordChangedConfirmation(user.getEmail()); // Or a specific "Admin Reset" email if we had one
            }).start();
            return tempPass;
        }
        return null;
    }

    /**
     * Gets user by password reset token.
     * @param token reset token
     * @return the User or null
     * @throws SQLException if query fails
     */
    public User getUserByResetToken(String token) throws SQLException {
        return userDAO.getUserByResetToken(token);
    }

    /**
     * Test helper to get reset token for email
     */
    public String getUserByResetTokenForTesting(String email) throws SQLException {
        User user = userDAO.getUserByEmail(email);
        return user != null ? user.getResetToken() : null;
    }

    // ==================== SPRINT 1: ACCOUNT LOCKOUT ====================

    /**
     * Gets login attempts for a user.
     * @param email user email
     * @return number of failed login attempts
     * @throws SQLException if query fails
     */
    public int getLoginAttempts(String email) throws SQLException {
        return userDAO.getLoginAttempts(email);
    }

    /**
     * Checks if an account is locked.
     * @param email user email
     * @return true if account is currently locked
     * @throws SQLException if query fails
     */
    public boolean isAccountLocked(String email) throws SQLException {
        return userDAO.isAccountLocked(email);
    }

    /**
     * Unlocks a user account (admin action).
     * @param email user email
     * @throws SQLException if operation fails
     */
    public void unlockAccount(String email) throws SQLException {
        userDAO.resetLoginAttempts(email);
    }

    // ==================== SPRINT 1: PROFESSIONAL VERIFICATION ====================

    /**
     * Updates user diploma URL.
     * @param uuid user UUID
     * @param diplomaUrl URL to diploma file
     * @return true if update successful
     * @throws SQLException if operation fails
     */
    public boolean updateDiplomaUrl(String uuid, String diplomaUrl) throws SQLException {
        return userDAO.updateDiplomaUrl(uuid, diplomaUrl);
    }

    /**
     * Manually verifies a professional user (admin action).
     * @param uuid user UUID
     * @return true if verification successful
     * @throws SQLException if operation fails
     */
    public boolean verifyProfessional(String uuid) throws SQLException {
        return userDAO.verifyProfessional(uuid);
    }

    /**
     * Unverifies a professional user (admin action).
     * @param uuid user UUID
     * @return true if unverification successful
     * @throws SQLException if operation fails
     */
    public boolean unverifyProfessional(String uuid) throws SQLException {
        return userDAO.unverifyProfessional(uuid);
    }

    /**
     * Gets users pending professional verification.
     * @return list of professional users not yet verified
     * @throws SQLException if query fails
     */
    public List<User> getPendingProfessionalVerification() throws SQLException {
        return userDAO.getPendingProfessionalVerification();
    }

    // ==================== REGISTRATION METHODS ====================

    /**
     * Registers a new patient user.
     * @param user the user object with patient information
     * @param plainPassword the plain text password
     * @return the registered user with generated UUID
     * @throws SQLException if registration fails
     * @throws IllegalArgumentException if validation fails
     */
    public User registerPatient(User user, String plainPassword) throws SQLException {
        // Validate user data
        String validationError = ValidationUtils.validateRegistration(
            user.getEmail(), plainPassword, plainPassword,
            user.getFirstName(), user.getLastName(), user.getPhone(),
            null, user.getRole()
        );
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Check email uniqueness
        if (userDAO.emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà enregistrée. Veuillez utiliser une autre adresse ou vous connecter.");
        }

        // Hash password
        String hashedPassword = hashPassword(plainPassword);

        // Create user in database
        User createdUser = userDAO.createUser(user, hashedPassword);
        
        if (createdUser != null) {
            // Generate verification token and send email asynchronously
            String token = userDAO.generateEmailVerificationToken(createdUser.getUuid());
            new Thread(() -> {
                emailService.sendVerificationEmail(createdUser.getEmail(), token);
            }).start();
        }
        
        return createdUser;
    }

    /**
     * Registers a new professional user with diploma file upload.
     * @param user the user data (password should be plain text)
     * @param diplomaFile the diploma/certification file to store
     * @return the registered user with generated UUID
     * @throws SQLException if registration fails
     * @throws IllegalArgumentException if validation fails
     */
    public User registerProfessional(User user, java.io.File diplomaFile) throws SQLException {
        // Validate user data
        String validationError = ValidationUtils.validateRegistration(
            user.getEmail(), user.getPassword(), user.getPassword(),
            user.getFirstName(), user.getLastName(), user.getPhone(),
            user.getLicenseNumber(), user.getRole()
        );
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Check email uniqueness
        if (userDAO.emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà enregistrée. Veuillez utiliser une autre adresse ou vous connecter.");
        }

        // Validate diploma file
        if (diplomaFile == null) {
            throw new IllegalArgumentException("Diploma file is required");
        }

        // Check file size (5MB limit)
        if (diplomaFile.length() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Diploma file size must be less than 5MB");
        }

        // Hash password
        String hashedPassword = hashPassword(user.getPassword());

        // Create user in database
        User createdUser = userDAO.createUser(user, hashedPassword);

        // Store diploma file
        String extension = "";
        int i = diplomaFile.getName().lastIndexOf('.');
        if (i > 0) extension = diplomaFile.getName().substring(i);
        
        // Step 1: Save Diploma File locally
        String diplomaFileName = "diploma_" + createdUser.getUuid() + extension;
        try {
            java.nio.file.Path targetDir = java.nio.file.Paths.get("uploads", "diplomas");
            if (!java.nio.file.Files.exists(targetDir)) {
                java.nio.file.Files.createDirectories(targetDir);
            }
            java.nio.file.Files.copy(diplomaFile.toPath(), targetDir.resolve(diplomaFileName), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            createdUser.setDiplomaUrl(diplomaFileName);
            userDAO.updateDiplomaUrl(createdUser.getUuid(), diplomaFileName);
        } catch (java.io.IOException e) {
            System.err.println("Failed to save diploma file: " + e.getMessage());
        }

        // Step 1: Create Initial Verification Record
        com.wellcare.javafx.model.ProfessionalVerification pv = new com.wellcare.javafx.model.ProfessionalVerification();
        pv.setProfessionalUuid(createdUser.getUuid());
        pv.setProfessionalEmail(createdUser.getEmail());
        pv.setLicenseNumber(createdUser.getLicenseNumber());
        pv.setSpecialty(createdUser.getSpecialite());
        pv.setDiplomaPath("uploads/diplomas/" + diplomaFileName);
        
        try {
            userDAO.createProfessionalVerification(pv);
        } catch (SQLException e) {
            System.err.println("Failed to create verification record: " + e.getMessage());
        }

        // Step 2: Trigger AI Verification Pipeline
        diplomaVerificationService.verifyDiploma(createdUser, diplomaFile)
            .thenAccept(result -> {
                try {
                    // Update verification model
                    pv.setConfidenceScore(result.score);
                    pv.setStatus(result.status);
                    
                    // Wrap raw text in JSON to satisfy MySQL JSON constraint
                    org.json.JSONObject extractedJson = new org.json.JSONObject();
                    extractedJson.put("raw_text", result.extractedText);
                    pv.setExtractedData(extractedJson.toString());
                    
                    pv.setValidationDetails(new org.json.JSONObject(result.details).toString());
                    pv.setForgeryIndicators(new org.json.JSONObject(result.forgeryIndicators).toString());
                    if (!"manual_review".equals(result.status)) {
                        pv.setVerifiedAt(java.time.LocalDateTime.now());
                    }

                    // Update professional_verifications table
                    System.out.println("AI Verification Complete for: " + createdUser.getEmail() + " | Status: " + result.status + " | ID: " + pv.getId());
                    userDAO.updateProfessionalVerificationResults(pv);

                    // Backward compatibility update for users table
                    userDAO.updateVerificationResults(
                        createdUser.getUuid(), 
                        result.score, 
                        result.status, 
                        result.extractedText
                    );

                    // Automatic Decision Logic
                    if ("verified".equals(result.status)) {
                        userDAO.updateVerificationStatus(createdUser.getUuid(), true);
                        emailService.sendSimpleEmail(createdUser.getEmail(), 
                            "Diploma Verified Successfully", 
                            "Great news! Our AI system has verified your diploma with a confidence score of " + result.score + "%. Your account is now fully active.");
                    } else if ("rejected".equals(result.status)) {
                        emailService.sendSimpleEmail(createdUser.getEmail(), 
                            "Diploma Verification Failed", 
                            "Unfortunately, our AI system could not verify your diploma (Score: " + result.score + "%). Our team will review it manually.");
                    }
                } catch (Exception e) {
                    System.err.println("Database error during AI verification update: " + e.getMessage());
                    e.printStackTrace();
                }
            })
            .exceptionally(ex -> {
                System.err.println("CRITICAL ERROR in AI Verification Pipeline: " + ex.getMessage());
                ex.printStackTrace();
                return null;
            });

        // Step 3: Generate verification token and send email
        if (createdUser != null) {
            try {
                String token = userDAO.generateEmailVerificationToken(createdUser.getUuid());
                new Thread(() -> {
                    emailService.sendVerificationEmail(createdUser.getEmail(), token);
                }).start();
            } catch (SQLException e) {
                System.err.println("Failed to generate verification token: " + e.getMessage());
            }
        }

        return createdUser;
    }

    /**
     * Gets the diploma file for a professional.
     * @param filename the relative filename stored in DB
     * @return the absolute file or null
     */
    public java.io.File getDiplomaFile(String filename) {
        if (filename == null || filename.isEmpty()) return null;
        java.io.File file = new java.io.File("uploads/diplomas/" + filename);
        return file.exists() ? file : null;
    }

    /**
     * Registers a new professional user (Physician, Coach, Nutritionist).
     * @param user the user object with professional information
     * @param plainPassword the plain text password
     * @return the registered user with generated UUID
     * @throws SQLException if registration fails
     * @throws IllegalArgumentException if validation fails
     */
    public User registerProfessional(User user, String plainPassword) throws SQLException {
        // Validate user data including license
        String validationError = ValidationUtils.validateRegistration(
            user.getEmail(), plainPassword, plainPassword,
            user.getFirstName(), user.getLastName(), user.getPhone(),
            user.getLicenseNumber(), user.getRole()
        );
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Check email uniqueness
        if (userDAO.emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà enregistrée. Veuillez utiliser une autre adresse ou vous connecter.");
        }

        // Hash password
        String hashedPassword = hashPassword(plainPassword);

        // Ensure professional fields are set properly
        user.setVerifiedByAdmin(false); // Professionals must be manually verified by admin

        // Create user in database
        User createdUser = userDAO.createUser(user, hashedPassword);
        
        if (createdUser != null) {
            // Generate verification token and send email asynchronously
            String token = userDAO.generateEmailVerificationToken(createdUser.getUuid());
            new Thread(() -> {
                emailService.sendVerificationEmail(createdUser.getEmail(), token);
            }).start();
        }
        
        return createdUser;
    }

    /**
     * Reprocesses a diploma using the AI pipeline (manually triggered by Admin).
     */
    public java.util.concurrent.CompletableFuture<DiplomaVerificationService.VerificationResult> reprocessDiplomaAi(User professional, java.io.File diplomaFile) {
        // Create a new verification record for this attempt
        com.wellcare.javafx.model.ProfessionalVerification pv = new com.wellcare.javafx.model.ProfessionalVerification();
        pv.setProfessionalUuid(professional.getUuid());
        pv.setProfessionalEmail(professional.getEmail());
        pv.setLicenseNumber(professional.getLicenseNumber());
        pv.setSpecialty(professional.getSpecialite());
        pv.setDiplomaPath("uploads/diplomas/" + diplomaFile.getName());
        pv.setStatus("processing");

        try {
            userDAO.createProfessionalVerification(pv);
        } catch (SQLException e) {
            System.err.println("Failed to create verification record: " + e.getMessage());
        }

        return diplomaVerificationService.verifyDiploma(professional, diplomaFile)
            .thenApply(result -> {
                try {
                    // Update verification record
                    pv.setConfidenceScore(result.score);
                    pv.setStatus(result.status);
                    
                    // Wrap raw text in JSON to satisfy MySQL JSON constraint
                    org.json.JSONObject extractedJson = new org.json.JSONObject();
                    extractedJson.put("raw_text", result.extractedText);
                    pv.setExtractedData(extractedJson.toString());
                    
                    pv.setValidationDetails(new org.json.JSONObject(result.details).toString());
                    pv.setForgeryIndicators(new org.json.JSONObject(result.forgeryIndicators).toString());
                    pv.setVerifiedAt(java.time.LocalDateTime.now());
                    userDAO.updateProfessionalVerificationResults(pv);

                    // Update user table for backward compatibility
                    userDAO.updateVerificationResults(professional.getUuid(), result.score, result.status, result.extractedText);
                    if ("verified".equals(result.status)) {
                        userDAO.updateVerificationStatus(professional.getUuid(), true);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("DB Error: " + e.getMessage());
                }
                return result;
            });
    }

    // ==================== GOOGLE OAUTH METHODS ====================

    /**
     * Processes Google Login logic: finds or creates user based on Google profile.
     * Matches the Web Sprint procedure (Step 7 & 8).
     * @param googleId the unique Google ID
     * @param email the user's email from Google
     * @param firstName the user's first name from Google
     * @param lastName the user's last name from Google
     * @param avatarUrl the profile picture URL from Google
     * @return the logged-in User
     * @throws SQLException if operation fails
     */
    public User processGoogleLogin(String googleId, String email, String firstName, String lastName, String avatarUrl) throws SQLException {
        // 1. Search by Google ID first
        User user = userDAO.getUserByGoogleId(googleId);
        
        if (user == null) {
            // 2. Search by Email
            user = userDAO.getUserByEmail(email);
            
            if (user == null) {
                // 3. Create new account (Default role: PATIENT as per Step 7)
                user = new User();
                user.setEmail(email);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setAvatarUrl(avatarUrl);
                user.setGoogleId(googleId);
                user.setRole("ROLE_PATIENT");
                user.setEmailVerified(true); // Google already verified
                user.setActive(true);
                
                // Set random password (not used for OAuth)
                String randomPass = java.util.UUID.randomUUID().toString();
                user = userDAO.createUser(user, hashPassword(randomPass));
            } else {
                // Link Google ID to existing account (Step 7 "Then by Google ID")
                userDAO.updateGoogleId(user.getUuid(), googleId);
                user.setGoogleId(googleId);
            }
        }
        
        // Step 8: Update user info with latest Google data
        user.setAvatarUrl(avatarUrl);
        user.setLastLoginAt(LocalDateTime.now());
        userDAO.resetLoginAttempts(user.getEmail());
        
        return user;
    }

    /**
     * Resets the entire login state for a user.
     */
    public void resetLoginState(String email) throws SQLException {
        userDAO.resetLoginAttempts(email);
    }

    // ==================== TWO-FACTOR AUTH ====================

    /**
     * Initiates 2FA setup by generating a secret and OTP URI.
     */
    public String initiate2FASetup(String uuid) throws SQLException {
        User user = userDAO.getUserByUuid(uuid);
        if (user == null) throw new IllegalArgumentException("Utilisateur non trouvé");
        
        TotpService totp = new TotpService();
        return totp.getOtpAuthUri(totp.generateSecret(), user.getEmail());
    }

    /**
     * Completes 2FA activation after verifying the first code.
     * Returns generated backup codes.
     */
    public List<String> activate2FA(String uuid, String secret, String code) throws SQLException {
        TotpService totp = new TotpService();
        if (totp.verifyCode(secret, code)) {
            List<String> backupCodes = totp.generateBackupCodes(10);
            userDAO.update2FA(uuid, true, secret, backupCodes);
            return backupCodes;
        }
        throw new IllegalArgumentException("Le code de vérification est incorrect");
    }

    /**
     * Verifies a 2FA code (TOTP or Backup code) during login.
     */
    public boolean verify2FA(User user, String code) throws SQLException {
        if (!user.isTwoFactorEnabled()) return true;
        
        // 1. Try TOTP
        TotpService totp = new TotpService();
        if (totp.verifyCode(user.getTotpSecret(), code)) {
            return true;
        }
        
        // 2. Try Backup Codes
        List<String> backupCodes = user.getBackupCodes();
        if (backupCodes != null && backupCodes.contains(code)) {
            backupCodes.remove(code);
            userDAO.update2FA(user.getUuid(), true, user.getTotpSecret(), backupCodes);
            return true;
        }
        
        return false;
    }

    /**
     * Disables 2FA for a user.
     */
    public boolean disable2FA(String uuid) throws SQLException {
        return userDAO.update2FA(uuid, false, null, null);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Hashes a password using BCrypt (PHP compatible $2y$ format).
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    private String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }
}
