package com.wellora.dao;

import com.wellora.model.Healthjournal;
import com.wellora.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * HealthjournalDAO — JDBC Data Access Object
 *
 * Symfony equivalent: src/Repository/HealthjournalRepository.php
 *
 * In Symfony, Doctrine generated SQL automatically from QueryBuilder.
 * Here we write SQL manually using PreparedStatements.
 *
 * Symfony → Java mapping:
 *   findAll()                      → findAll()
 *   find($id)                      → findById(int id)
 *   findOneBy(['name' => $name])   → findByName(String name)
 *   QueryBuilder LIKE search       → search(String name, LocalDate filterDate)
 *   persist() + flush()            → save(Healthjournal)
 *   remove() + flush()             → delete(int id)
 */
public class HealthjournalDAO {

    // ----------------------------------------------------------------
    // findAll — Symfony: findAll()
    // ----------------------------------------------------------------
    public List<Healthjournal> findAll() throws SQLException {
        String sql = "SELECT * FROM healthjournal ORDER BY datedebut ASC";
        List<Healthjournal> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // findById — Symfony: find($id)
    // ----------------------------------------------------------------
    public Optional<Healthjournal> findById(int id) throws SQLException {
        String sql = "SELECT * FROM healthjournal WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // ----------------------------------------------------------------
    // findByName — Symfony: findOneBy(['name' => $name])
    // ----------------------------------------------------------------
    public Optional<Healthjournal> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM healthjournal WHERE name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // ----------------------------------------------------------------
    // search — Symfony: QueryBuilder with LIKE + date filter + sort
    //
    // Symfony:
    //   ->andWhere('h.name LIKE :search')
    //   ->andWhere('h.datedebut >= :filterDate')
    //   ->orderBy('h.' . $sortBy, $sortOrder)
    // ----------------------------------------------------------------
    public List<Healthjournal> search(String name, LocalDate filterDate,
                                      String sortBy, String sortOrder) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM healthjournal WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + name + "%");
        }
        if (filterDate != null) {
            sql.append(" AND datedebut >= ?");
            params.add(Date.valueOf(filterDate));
        }

        // Whitelist sortBy to prevent SQL injection
        String safeSort = List.of("name", "datedebut", "datefin").contains(sortBy) ? sortBy : "datedebut";
        String safeOrder = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(safeSort).append(" ").append(safeOrder);

        List<Healthjournal> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // save — Symfony: persist($journal) + flush()
    //   INSERT if id == 0, UPDATE otherwise
    // ----------------------------------------------------------------
    public void save(Healthjournal journal) throws SQLException {
        if (journal.getId() == 0) {
            insert(journal);
        } else {
            update(journal);
        }
    }

    private void insert(Healthjournal journal) throws SQLException {
        String sql = "INSERT INTO healthjournal (name, datedebut, datefin) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, journal.getName());
            ps.setDate(2, Date.valueOf(journal.getDatedebut()));
            ps.setDate(3, Date.valueOf(journal.getDatefin()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) journal.setId(keys.getInt(1));
            }
        }
    }

    private void update(Healthjournal journal) throws SQLException {
        String sql = "UPDATE healthjournal SET name=?, datedebut=?, datefin=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, journal.getName());
            ps.setDate(2, Date.valueOf(journal.getDatedebut()));
            ps.setDate(3, Date.valueOf(journal.getDatefin()));
            ps.setInt(4, journal.getId());
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // delete — Symfony: remove($journal) + flush()
    // Cascades: delete all related entries and their symptoms first
    // ----------------------------------------------------------------
    public void delete(int id) throws SQLException {
        // First delete all related entries (which will cascade delete symptoms)
        String deleteEntriesSql = "DELETE FROM healthentry WHERE journal_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteEntriesSql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        // Now delete the journal itself
        String sql = "DELETE FROM healthjournal WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // mapRow — maps a ResultSet row to a Healthjournal object
    // Symfony equivalent: Doctrine's hydration
    // ----------------------------------------------------------------
    private Healthjournal mapRow(ResultSet rs) throws SQLException {
        return new Healthjournal(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDate("datedebut").toLocalDate(),
                rs.getDate("datefin").toLocalDate()
        );
    }

    // ----------------------------------------------------------------
    // findByDate — finds a journal that contains the given date
    // Used for auto-assigning journal when creating entries
    // ----------------------------------------------------------------
    public Optional<Healthjournal> findByDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM healthjournal WHERE datedebut <= ? AND datefin >= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // ----------------------------------------------------------------
    // Analytics methods for Dashboard
    // ----------------------------------------------------------------

    /** Get total count of health journals */
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM healthjournal";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
