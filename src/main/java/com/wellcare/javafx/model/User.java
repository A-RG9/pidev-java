package com.wellcare.javafx.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User entity matching the web sprint User.php exactly.
 * Supports multiple roles: Patient, Doctor, Coach, Nutritionist, Admin.
 */
public class User {
    private String uuid;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate birthdate;
    private String phone;
    private String avatarUrl;
    private String address;
    private String role;
    private String licenseNumber;
    private String specialite;
    private boolean isActive;
    private boolean isEmailVerified;
    private boolean isVerifiedByAdmin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String resetToken;
    private LocalDateTime resetTokenExpiresAt;
    private LocalDateTime lastLoginAt;
    private int loginAttempts;
    private LocalDateTime lockedUntil;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationExpiresAt;
    private String lastSessionId;
    private String googleId;
    private boolean isTwoFactorEnabled;
    private String totpSecret;
    private List<String> backupCodes;
    private List<String> plainBackupCodes;
    private List<String> trustedDevices;
    private int yearsOfExperience;
    private String diplomaUrl;
    private LocalDateTime verificationDate;
    private String about;
    private String education;
    private String certifications;
    private String hospitalAffiliations;
    private String awards;
    private List<String> specializations;
    private Integer consultationPrice;
    private String lot;
    private String token;
    private Double rating;
    private Integer verificationScore;
    private String verificationDescription;

    /**
     * Default constructor.
     */
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.isEmailVerified = false;
        this.isVerifiedByAdmin = false;
        this.loginAttempts = 0;
        this.yearsOfExperience = 0;
        this.consultationPrice = 120;
    }

    /**
     * Constructor with basic user information.
     */
    public User(String email, String password, String firstName, String lastName, String role) {
        this();
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // ==================== BASIC FIELDS ====================

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // ==================== PROFESSIONAL FIELDS ====================

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    // Legacy getter for compatibility
    public String getSpecialty() { return specialite; }
    public void setSpecialty(String specialty) { this.specialite = specialty; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getDiplomaUrl() { return diplomaUrl; }
    public void setDiplomaUrl(String diplomaUrl) { this.diplomaUrl = diplomaUrl; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public String getHospitalAffiliations() { return hospitalAffiliations; }
    public void setHospitalAffiliations(String hospitalAffiliations) { this.hospitalAffiliations = hospitalAffiliations; }

    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }

    public List<String> getSpecializations() { return specializations; }
    public void setSpecializations(List<String> specializations) { this.specializations = specializations; }

    public Integer getConsultationPrice() { return consultationPrice; }
    public void setConsultationPrice(Integer consultationPrice) { this.consultationPrice = consultationPrice; }

    // ==================== STATUS FIELDS ====================

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean active) { isActive = active; }

    public boolean isEmailVerified() { return isEmailVerified; }
    public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }
    public boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }

    public boolean isVerifiedByAdmin() { return isVerifiedByAdmin; }
    public void setVerifiedByAdmin(boolean verifiedByAdmin) { isVerifiedByAdmin = verifiedByAdmin; }
    public boolean getIsVerifiedByAdmin() { return isVerifiedByAdmin; }
    public void setIsVerifiedByAdmin(boolean verifiedByAdmin) { isVerifiedByAdmin = verifiedByAdmin; }

    // ==================== TIMESTAMP FIELDS ====================

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getVerificationDate() { return verificationDate; }
    public void setVerificationDate(LocalDateTime verificationDate) { this.verificationDate = verificationDate; }

    // ==================== PASSWORD RESET FIELDS ====================

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }

    // ==================== LOGIN TRACKING FIELDS ====================

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    // Alias methods for compatibility with UI controllers
    public LocalDateTime getLastLogin() { return getLastLoginAt(); }
    public void setLastLogin(LocalDateTime lastLogin) { setLastLoginAt(lastLogin); }

    public int getLoginAttempts() { return loginAttempts; }
    public void setLoginAttempts(int loginAttempts) { this.loginAttempts = loginAttempts; }

    public void incrementLoginAttempts() { this.loginAttempts++; }
    public void resetLoginAttempts() { this.loginAttempts = 0; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    // ==================== EMAIL VERIFICATION FIELDS ====================

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) { this.emailVerificationToken = emailVerificationToken; }

    public LocalDateTime getEmailVerificationExpiresAt() { return emailVerificationExpiresAt; }
    public void setEmailVerificationExpiresAt(LocalDateTime emailVerificationExpiresAt) { this.emailVerificationExpiresAt = emailVerificationExpiresAt; }

    // ==================== SESSION & OAUTH FIELDS ====================

    public String getLastSessionId() { return lastSessionId; }
    public void setLastSessionId(String lastSessionId) { this.lastSessionId = lastSessionId; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    // ==================== TWO-FACTOR AUTH FIELDS ====================

    public boolean isTwoFactorEnabled() { return isTwoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { isTwoFactorEnabled = twoFactorEnabled; }
    public boolean getIsTwoFactorEnabled() { return isTwoFactorEnabled; }
    public void setIsTwoFactorEnabled(boolean twoFactorEnabled) { isTwoFactorEnabled = twoFactorEnabled; }

    public String getTotpSecret() { return totpSecret; }
    public void setTotpSecret(String totpSecret) { this.totpSecret = totpSecret; }

    public List<String> getBackupCodes() { return backupCodes; }
    public void setBackupCodes(List<String> backupCodes) { this.backupCodes = backupCodes; }

    public List<String> getPlainBackupCodes() { return plainBackupCodes; }
    public void setPlainBackupCodes(List<String> plainBackupCodes) { this.plainBackupCodes = plainBackupCodes; }

    public List<String> getTrustedDevices() { return trustedDevices; }
    public void setTrustedDevices(List<String> trustedDevices) { this.trustedDevices = trustedDevices; }

    // ==================== ADDITIONAL FIELDS ====================

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    // ==================== HELPER METHODS ====================

    /**
     * Returns the full name of the user.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns a display-friendly role name.
     */
    public String getDisplayRole() {
        if (role == null) return "Unknown";
        return switch (role) {
            case "ROLE_PATIENT" -> "Patient";
            case "ROLE_MEDECIN" -> "Doctor";
            case "ROLE_COACH" -> "Coach";
            case "ROLE_NUTRITIONIST" -> "Nutritionist";
            case "ROLE_ADMIN" -> "Administrator";
            default -> {
                String cleaned = role.replace("ROLE_", "").toLowerCase();
                yield cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
            }
        };
    }

    @Override
    public String toString() {
        return getFullName() + " (" + email + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return uuid != null && uuid.equals(user.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    public Integer getVerificationScore() { return verificationScore; }
    public void setVerificationScore(Integer verificationScore) { this.verificationScore = verificationScore; }

    public String getVerificationDescription() { return verificationDescription; }
    public void setVerificationDescription(String verificationDescription) { this.verificationDescription = verificationDescription; }
}
