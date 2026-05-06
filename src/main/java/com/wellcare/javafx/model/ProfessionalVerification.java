package com.wellcare.javafx.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing a professional verification attempt.
 */
public class ProfessionalVerification {
    private Integer id;
    private String professionalUuid;
    private String professionalEmail;
    private String licenseNumber;
    private String specialty;
    private String diplomaPath;
    private String diplomaFilename;
    private String extractedData; // Storage as JSON string
    private Integer confidenceScore;
    private String status; // pending, processing, verified, rejected, manual_review
    private String validationDetails; // JSON string
    private String forgeryIndicators; // JSON string
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private String reviewedBy;

    // Status Constants
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_VERIFIED = "verified";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_MANUAL_REVIEW = "manual_review";

    public ProfessionalVerification() {
        this.status = STATUS_PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getProfessionalUuid() { return professionalUuid; }
    public void setProfessionalUuid(String professionalUuid) { this.professionalUuid = professionalUuid; }

    public String getProfessionalEmail() { return professionalEmail; }
    public void setProfessionalEmail(String professionalEmail) { this.professionalEmail = professionalEmail; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getDiplomaPath() { return diplomaPath; }
    public void setDiplomaPath(String diplomaPath) { this.diplomaPath = diplomaPath; }

    public String getDiplomaFilename() { return diplomaFilename; }
    public void setDiplomaFilename(String diplomaFilename) { this.diplomaFilename = diplomaFilename; }

    public String getExtractedData() { return extractedData; }
    public void setExtractedData(String extractedData) { this.extractedData = extractedData; }

    public Integer getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getValidationDetails() { return validationDetails; }
    public void setValidationDetails(String validationDetails) { this.validationDetails = validationDetails; }

    public String getForgeryIndicators() { return forgeryIndicators; }
    public void setForgeryIndicators(String forgeryIndicators) { this.forgeryIndicators = forgeryIndicators; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
}
