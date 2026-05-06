package com.wellora.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Healthjournal Model (POJO)
 *
 * Symfony equivalent: src/Entity/Healthjournal.php
 *
 * No JPA annotations here — this is a plain Java object.
 * Database mapping is handled manually in HealthjournalDAO via JDBC.
 */
public class Healthjournal {

    private int id;
    private String name;
    private LocalDate datedebut;
    private LocalDate datefin;
    private List<Healthentry> entries = new ArrayList<>();

    // ---- Constructors ----

    public Healthjournal() {}

    public Healthjournal(int id, String name, LocalDate datedebut, LocalDate datefin) {
        this.id = id;
        this.name = name;
        this.datedebut = datedebut;
        this.datefin = datefin;
    }

    // ---- Symfony equivalent: getEntriesByDateRange() ----

    public List<Healthentry> getEntriesByDateRange() {
        if (datedebut == null || datefin == null) return entries;
        return entries.stream()
                .filter(e -> e.getDate() != null
                        && !e.getDate().isBefore(datedebut)
                        && !e.getDate().isAfter(datefin))
                .collect(Collectors.toList());
    }

    // ---- Getters & Setters ----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDatedebut() { return datedebut; }
    public void setDatedebut(LocalDate datedebut) { this.datedebut = datedebut; }

    public LocalDate getDatefin() { return datefin; }
    public void setDatefin(LocalDate datefin) { this.datefin = datefin; }

    public List<Healthentry> getEntries() { return entries; }
    public void setEntries(List<Healthentry> entries) { this.entries = entries; }
}
