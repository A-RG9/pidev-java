package com.wellora.dao;

import com.wellora.model.Symptom;
import com.wellora.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SymptomDAO {

    public List<Symptom> findAll() throws SQLException {
        List<Symptom> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM symptom");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Symptom> findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM symptom WHERE id=?")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Symptom> findByEntryId(int entryId) throws SQLException {
        List<Symptom> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM symptom WHERE entry_id=?")) {

            ps.setInt(1, entryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    public void save(Symptom symptom) throws SQLException {
        if (symptom.getId() == 0) {
            insert(symptom);
        } else {
            update(symptom);
        }
    }

    private void insert(Symptom symptom) throws SQLException {
        String sql = "INSERT INTO symptom (type, intensite, zone, entry_id) VALUES (?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, symptom.getType());
            ps.setInt(2, symptom.getIntensite());
            ps.setString(3, symptom.getZone());
            ps.setInt(4, symptom.getEntryId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    symptom.setId(keys.getInt(1));
                }
            }
        }
    }

    private void update(Symptom symptom) throws SQLException {
        String sql = "UPDATE symptom SET type=?, intensite=?, zone=?, entry_id=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, symptom.getType());
            ps.setInt(2, symptom.getIntensite());
            ps.setString(3, symptom.getZone());
            ps.setInt(4, symptom.getEntryId());
            ps.setInt(5, symptom.getId());

            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM symptom WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteByEntryId(int entryId) throws SQLException {
        String sql = "DELETE FROM symptom WHERE entry_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, entryId);
            ps.executeUpdate();
        }
    }

    private Symptom mapRow(ResultSet rs) throws SQLException {
        return new Symptom(
                rs.getInt("id"),
                rs.getString("type"),
                rs.getInt("intensite"),
                rs.getString("zone"),
                rs.getInt("entry_id")
        );
    }

    // ---------------- Analytics ----------------

    public int getCount(String userId) throws SQLException {
        String sql = "SELECT COUNT(s.id) FROM symptom s " +
                     "JOIN healthentry e ON s.entry_id = e.id " +
                     "JOIN healthjournal j ON e.journal_id = j.id " +
                     "WHERE j.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Object[]> getCountByType(String userId) throws SQLException {
        String sql = "SELECT s.type, COUNT(*) as count FROM symptom s " +
                     "JOIN healthentry e ON s.entry_id = e.id " +
                     "JOIN healthjournal j ON e.journal_id = j.id " +
                     "WHERE j.user_id = ? " +
                     "GROUP BY s.type ORDER BY count DESC";

        List<Object[]> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                            rs.getString("type"),
                            rs.getLong("count")
                    });
                }
            }
        }
        return result;
    }

    public int getCountByJournal(int journalId) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM symptom s " +
                        "INNER JOIN healthentry e ON s.entry_id = e.id " +
                        "WHERE e.journal_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, journalId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Object[]> getCountByTypeByJournal(int journalId) throws SQLException {
        String sql =
                "SELECT s.type, COUNT(*) as count FROM symptom s " +
                        "INNER JOIN healthentry e ON s.entry_id = e.id " +
                        "WHERE e.journal_id = ? " +
                        "GROUP BY s.type ORDER BY count DESC";

        List<Object[]> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, journalId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                            rs.getString("type"),
                            rs.getLong("count")
                    });
                }
            }
        }
        return result;
    }
}