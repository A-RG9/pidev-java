package com.wellcare.javafx.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;
import javafx.scene.Node;

/**
 * Utility class for input validation (contrôle de saisie).
 */
public class ValidationUtils {

    // ==================== REGEX PATTERNS ====================
    
    /**
     * Email pattern: ^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$
     */
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    
    /**
     * Phone pattern: /^[+]?[0-9\s\-()]+$/
     */
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9\\s\\-()]+$");
    
    /**
     * Password patterns
     */
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
    
    /**
     * Name pattern: 2-100 characters
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]{2,100}$");
    
    /**
     * License number pattern
     */
    private static final Pattern LICENSE_PATTERN = Pattern.compile("^[A-Z0-9\\-]{5,50}$");

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates email format.
     * @param email the email to validate
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates phone number format.
     * @param phone the phone to validate
     * @return true if valid or null/empty
     */
    public static boolean isValidPhone(String phone) {
        return phone == null || phone.trim().isEmpty() || PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates name (first name or last name).
     * @param name the name to validate
     * @return true if valid
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates license number format.
     * @param license the license number to validate
     * @return true if valid or null/empty
     */
    public static boolean isValidLicenseNumber(String license) {
        return license == null || license.trim().isEmpty() || LICENSE_PATTERN.matcher(license.trim()).matches();
    }

    /**
     * Validates password strength.
     * @param password the password to validate
     * @return true if meets all requirements
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return PASSWORD_UPPERCASE.matcher(password).find()
            && PASSWORD_LOWERCASE.matcher(password).find()
            && PASSWORD_DIGIT.matcher(password).find()
            && PASSWORD_SPECIAL.matcher(password).find();
    }

    /**
     * Gets password strength level.
     * @param password the password to check
     * @return "Weak", "Medium", or "Strong"
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Weak";
        }
        
        int score = 0;
        if (password.length() >= 8) score++;
        if (PASSWORD_UPPERCASE.matcher(password).find()) score++;
        if (PASSWORD_LOWERCASE.matcher(password).find()) score++;
        if (PASSWORD_DIGIT.matcher(password).find()) score++;
        if (PASSWORD_SPECIAL.matcher(password).find()) score++;
        
        if (score <= 2) return "Weak";
        if (score <= 4) return "Medium";
        return "Strong";
    }

    /**
     * Gets password strength score (0-5).
     * @param password the password to check
     * @return score from 0 to 5
     */
    public static int getPasswordStrengthScore(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        if (password.length() >= 8) score++;
        if (PASSWORD_UPPERCASE.matcher(password).find()) score++;
        if (PASSWORD_LOWERCASE.matcher(password).find()) score++;
        if (PASSWORD_DIGIT.matcher(password).find()) score++;
        if (PASSWORD_SPECIAL.matcher(password).find()) score++;
        
        return score;
    }

    // ==================== INDIVIDUAL FIELD VALIDATION ====================

    /**
     * Validates name with custom error message.
     * @param name the name to validate
     * @param fieldName the field name for error message
     * @return null if valid, error message if invalid
     */
    public static String validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            return fieldName + " is required";
        }
        if (!isValidName(name)) {
            return fieldName + " must be 2-100 characters and contain only letters, spaces, hyphens, and apostrophes";
        }
        return null;
    }

    /**
     * Validates email with error message.
     * @param email the email to validate
     * @return null if valid, error message if invalid
     */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }
        return null;
    }

    /**
     * Validates phone with error message.
     * @param phone the phone to validate
     * @return null if valid, error message if invalid
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // Phone is optional
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return "Please enter a valid phone number";
        }
        return null;
    }

    /**
     * Calculates password strength as a normalized value between 0 and 1.
     * @param password the password to evaluate
     * @return strength value between 0.0 (weak) and 1.0 (strong)
     */
    public static double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0.0;
        }

        int score = getPasswordStrengthScore(password);
        return (double) score / 5.0; // Normalize to 0-1 range
    }

    /**
     * Validates password strength with error message.
     * @param password the password to validate
     * @return null if valid, error message if invalid
     */
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (!PASSWORD_UPPERCASE.matcher(password).find()) {
            return "Password must contain at least one uppercase letter";
        }
        if (!PASSWORD_LOWERCASE.matcher(password).find()) {
            return "Password must contain at least one lowercase letter";
        }
        if (!PASSWORD_DIGIT.matcher(password).find()) {
            return "Password must contain at least one number";
        }
        if (!PASSWORD_SPECIAL.matcher(password).find()) {
            return "Password must contain at least one special character";
        }
        return null;
    }

    /**
     * Validates password confirmation.
     * @param password the original password
     * @param confirmPassword the confirmation password
     * @return null if valid, error message if invalid
     */
    public static String validateConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Please confirm your password";
        }
        if (!confirmPassword.equals(password)) {
            return "Passwords do not match";
        }
        return null;
    }

    /**
     * Validates login credentials.
     * @param email the email
     * @param password the password
     * @return null if valid, error message if invalid
     */
    public static String validateLoginCredentials(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        return null;
    }

    // ==================== REGISTRATION VALIDATION ====================

    /**
     * Validates all registration fields.
     * @param email user email
     * @param password user password
     * @param confirmPassword password confirmation
     * @param firstName user first name
     * @param lastName user last name
     * @param phone user phone (optional)
     * @param licenseNumber license number (required for professionals)
     * @param role user role
     * @return null if valid, or error message if invalid
     */
    public static String validateRegistration(String email, String password, String confirmPassword,
                                               String firstName, String lastName, String phone,
                                               String licenseNumber, String role) {
        // Email validation
        if (email == null || email.trim().isEmpty()) {
            return "Please enter your email address";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }

        // Password validation
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!PASSWORD_UPPERCASE.matcher(password).find()) {
            return "Password must contain at least one uppercase letter";
        }
        if (!PASSWORD_LOWERCASE.matcher(password).find()) {
            return "Password must contain at least one lowercase letter";
        }
        if (!PASSWORD_DIGIT.matcher(password).find()) {
            return "Password must contain at least one number";
        }
        if (!PASSWORD_SPECIAL.matcher(password).find()) {
            return "Password must contain at least one special character";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        
        // Name validation
        if (firstName == null || firstName.trim().isEmpty()) {
            return "First name is required";
        }
        if (firstName.trim().length() < 2) {
            return "First name must be at least 2 characters";
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            return "Last name is required";
        }
        if (lastName.trim().length() < 2) {
            return "Last name must be at least 2 characters";
        }
        
        // Phone validation (optional)
        if (phone != null && !phone.trim().isEmpty() && !isValidPhone(phone)) {
            return "Phone number must contain only digits and + - ( )";
        }
        
        // Professional validation
        if (!"ROLE_PATIENT".equals(role)) {
            if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
                return "License number is required for professionals";
            }
            if (!isValidLicenseNumber(licenseNumber)) {
                return "Invalid license number format";
            }
        }
        
        return null; // All valid
    }

    // ==================== LOGIN VALIDATION ====================

    /**
     * Validates login fields.
     * @param email user email
     * @param password user password
     * @return null if valid, or error message if invalid
     */
    public static String validateLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return "Please enter your email address";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }
        if (password == null || password.isEmpty()) {
            return "Please enter your password";
        }
        return null; // All valid
    }

    // ==================== PROFILE VALIDATION ====================

    /**
     * Validates profile edit fields.
     * @param firstName user first name
     * @param lastName user last name
     * @param phone user phone (optional)
     * @param address user address (optional)
     * @return null if valid, or error message if invalid
     */
    public static String validateProfile(String firstName, String lastName, String phone, String address) {
        // First name validation
        if (firstName == null || firstName.trim().isEmpty()) {
            return "First name is required";
        }
        if (firstName.trim().length() < 2) {
            return "First name must be at least 2 characters";
        }
        if (firstName.trim().length() > 100) {
            return "First name must be at most 100 characters";
        }
        
        // Last name validation
        if (lastName == null || lastName.trim().isEmpty()) {
            return "Last name is required";
        }
        if (lastName.trim().length() < 2) {
            return "Last name must be at least 2 characters";
        }
        if (lastName.trim().length() > 100) {
            return "Last name must be at most 100 characters";
        }
        
        // Phone validation (optional)
        if (phone != null && !phone.trim().isEmpty()) {
            if (phone.trim().length() > 20) {
                return "Phone number must be at most 20 characters";
            }
            if (!isValidPhone(phone)) {
                return "Invalid phone format";
            }
        }
        
        // Address validation (optional)
        if (address != null && !address.trim().isEmpty() && address.trim().length() > 255) {
            return "Address must be at most 255 characters";
        }
        
        return null; // All valid
    }

    // ==================== PROFESSIONAL VALIDATION ====================

    /**
     * Validates professional personal information fields.
     * @param firstName first name
     * @param lastName last name
     * @param email email address
     * @param phone phone number (optional)
     * @param birthDate birth date
     * @return null if valid, or error message if invalid
     */
    public static String validateProfessionalPersonalInfo(String firstName, String lastName, String email, String phone, java.time.LocalDate birthDate) {
        // First name validation
        if (firstName == null || firstName.trim().isEmpty()) {
            return "Le prénom est obligatoire";
        }
        if (firstName.length() < 2) {
            return "Le prénom doit contenir au moins 2 caractères";
        }

        // Last name validation
        if (lastName == null || lastName.trim().isEmpty()) {
            return "Le nom est obligatoire";
        }
        if (lastName.length() < 2) {
            return "Le nom doit contenir au moins 2 caractères";
        }

        // Email validation
        if (email == null || email.trim().isEmpty()) {
            return "L'email est obligatoire";
        }
        if (!isValidEmail(email)) {
            return "Format d'email invalide";
        }

        // Phone validation (optional)
        if (phone != null && !phone.trim().isEmpty() && !isValidPhone(phone)) {
            return "Format de numéro de téléphone invalide";
        }

        // Birth date validation
        if (birthDate == null) {
            return "La date de naissance est obligatoire";
        }
        if (birthDate.isAfter(java.time.LocalDate.now().minusYears(18))) {
            return "Vous devez avoir au moins 18 ans";
        }

        return null; // All valid
    }

    /**
     * Validates password and confirmation.
     * @param password password
     * @param confirmPassword password confirmation
     * @return null if valid, or error message if invalid
     */
    public static String validatePassword(String password, String confirmPassword) {
        if (password == null || password.trim().isEmpty()) {
            return "Le mot de passe est obligatoire";
        }
        if (password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères";
        }
        if (!password.equals(confirmPassword)) {
            return "Les mots de passe ne correspondent pas";
        }

        // Check password strength criteria
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            return "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial";
        }

        return null; // All valid
    }


    /**
     * Validates professional registration fields.
     * @param licenseNumber license number
     * @param yearsOfExperience years of experience
     * @param specialization specialization
     * @return null if valid, or error message if invalid
     */
    public static String validateProfessional(String licenseNumber, Integer yearsOfExperience, String specialization) {
        // License number validation
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            return "License number is required";
        }
        if (!isValidLicenseNumber(licenseNumber)) {
            return "Invalid license number format";
        }

        // Years of experience validation
        if (yearsOfExperience != null && yearsOfExperience < 0) {
            return "Years of experience must be positive";
        }

        // Specialization validation
        if (specialization == null || specialization.trim().isEmpty()) {
            return "Specialization is required";
        }

        return null; // All valid
    }

    /**
     * Checks if the person is at least the target age.
     * @param birthDate birth date
     * @param targetAge minimum required age
     * @return null if valid, error message if too young
     */
    public static String validateMinAge(LocalDate birthDate, int targetAge) {
        if (birthDate == null) {
            return "Birth date is required";
        }
        if (Period.between(birthDate, LocalDate.now()).getYears() < targetAge) {
            return "You must be at least " + targetAge + " years old";
        }
        return null;
    }

    /**
     * Validates a numeric range.
     */
    public static String validateNumericRange(String value, double min, double max, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " is required";
        }
        try {
            double val = Double.parseDouble(value.trim());
            if (val < min || val > max) {
                return fieldName + " must be between " + min + " and " + max;
            }
            return null;
        } catch (NumberFormatException e) {
            return fieldName + " must be a valid number";
        }
    }

    /**
     * Applies validation styles to a node based on validity.
     */
    public static void applyValidationStyle(Node node, boolean isValid) {
        if (isValid) {
            node.getStyleClass().remove("invalid");
            if (!node.getStyleClass().contains("valid")) {
                node.getStyleClass().add("valid");
            }
        } else {
            node.getStyleClass().remove("valid");
            if (!node.getStyleClass().contains("invalid")) {
                node.getStyleClass().add("invalid");
            }
        }
    }

    /**
     * Clears validation styles from a node.
     */
    public static void clearValidationStyle(Node node) {
        node.getStyleClass().remove("valid");
        node.getStyleClass().remove("invalid");
    }
}
