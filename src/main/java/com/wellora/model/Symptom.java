package com.wellora.model;

/**
 * Symptom Model (POJO)
 *
 * Symfony equivalent: src/Entity/Symptom.php
 *
 * Fields:
 *   type      → String  (e.g. "Céphalée", "Nausée", ...)
 *   intensite → int     (1–10)
 *   zone      → String  (nullable, body area)
 *   entryId   → int     (FK to healthentry)
 */
public class Symptom {

    private int id;
    private String type;
    private int intensite;
    private String zone;
    private int entryId;

    // Symptom type choices (from SymptomType.php ChoiceType)
    public static final String[] TYPE_CHOICES = {
        "Céphalée", "Nausée", "Vertige", "Douleurs musculaires",
        "Douleurs articulaires", "Fatigue", "Fièvre", "Toux",
        "Difficultés respiratoires", "Douleurs thoraciques",
        "Insomnie", "Anxiété", "Dépression", "Brûlure d'estomac",
        "Diarrhée", "Constipation", "Douleurs abdominales", "Autre"
    };

    // ---- Constructors ----

    public Symptom() {}

    public Symptom(int id, String type, int intensite, String zone, int entryId) {
        this.id = id;
        this.type = type;
        this.intensite = intensite;
        this.zone = zone;
        this.entryId = entryId;
    }

    // ---- Getters & Setters ----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getIntensite() { return intensite; }
    public void setIntensite(int intensite) { this.intensite = intensite; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public int getEntryId() { return entryId; }
    public void setEntryId(int entryId) { this.entryId = entryId; }
}
