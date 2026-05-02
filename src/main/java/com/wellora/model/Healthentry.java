package com.wellora.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Healthentry Model (POJO)
 *
 * Symfony equivalent: src/Entity/Healthentry.php
 *
 * Fields:
 *   date      → LocalDate  (was PHP \DateTime)
 *   poids     → double     (weight in kg, 30–200)
 *   glycemie  → double     (blood sugar g/l, 0.5–3)
 *   tension   → String     (blood pressure mmHg, 40–120)
 *   sommeil   → int        (sleep hours, 0–12)
 *   journal   → Healthjournal (ManyToOne)
 *   symptoms  → List<Symptom> (OneToMany)
 */
public class Healthentry {

    private int id;
    private LocalDate date;
    private double poids;
    private double glycemie;
    private String tension;
    private int sommeil;
    private int journalId;              // FK stored as int for JDBC
    private Healthjournal journal;      // loaded when needed
    private List<Symptom> symptoms = new ArrayList<>();

    // ---- Constructors ----

    public Healthentry() {}

    public Healthentry(int id, LocalDate date, double poids, double glycemie,
                       String tension, int sommeil, int journalId) {
        this.id = id;
        this.date = date;
        this.poids = poids;
        this.glycemie = glycemie;
        this.tension = tension;
        this.sommeil = sommeil;
        this.journalId = journalId;
    }

    // ---- Getters & Setters ----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getPoids() { return poids; }
    public void setPoids(double poids) { this.poids = poids; }

    public double getGlycemie() { return glycemie; }
    public void setGlycemie(double glycemie) { this.glycemie = glycemie; }

    public String getTension() { return tension; }
    public void setTension(String tension) { this.tension = tension; }

    public int getSommeil() { return sommeil; }
    public void setSommeil(int sommeil) { this.sommeil = sommeil; }

    public int getJournalId() { return journalId; }
    public void setJournalId(int journalId) { this.journalId = journalId; }

    public Healthjournal getJournal() { return journal; }
    public void setJournal(Healthjournal journal) {
        this.journal = journal;
        if (journal != null) this.journalId = journal.getId();
    }

    public List<Symptom> getSymptoms() { return symptoms; }
    public void setSymptoms(List<Symptom> symptoms) { this.symptoms = symptoms; }
}
