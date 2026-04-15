package com.wellora.dao;

import com.wellora.model.Healthentry;
import com.wellora.model.Healthjournal;
import com.wellora.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * HealthentryDAO — JDBC Data Access Object
 *
 * Symfony equivalent: src/Repository/HealthentryRepository.php
 *
 * Replaces Doctrine QueryBuilder methods:
 *   findByJournalAndDateRange()   → findByJournalAndDateRange()
 *   findByMonth()                 → findByMonth()
 *   findOneBy([...])              → findByDateAndJournal()
 *   QueryBuilder search           → search()
 */
public class HealthentryDAO {

    private final SymptomDAO symptomDAO = new SymptomDAO();

    // ----------------------------------------------------------------
    // findAll
    // ----------------------------------------------------------------
    public List<Healthentry> findAll() throws SQLException {
        String sql = "SELECT * FROM healthentry ORDER BY date DESC";
        List<Healthentry> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ----------------------------------------------------------------
    // findByJournalId - find entries by journal
    // ----------------------------------------------------------------
    public List<Healthentry> findByJournalId(int journalId) throws SQLException {
        String sql = "SELECT * FROM healthentry WHERE journal_id = ? ORDER BY date DESC";
        List<Healthentry> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // findById
    // ----------------------------------------------------------------
    public Optional<Healthentry> findById(int id) throws SQLException {
        String sql = "SELECT * FROM healthentry WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Healthentry entry = mapRow(rs);
                    entry.setSymptoms(symptomDAO.findByEntryId(id));
                    return Optional.of(entry);
                }
            }
        }
        return Optional.empty();
    }

    // ----------------------------------------------------------------
    // findByJournalAndDateRange — Symfony: findByJournalAndDateRange()
    //
    // Symfony QueryBuilder:
    //   ->andWhere('h.journal = :journal')
    //   ->andWhere('h.date >= :startDate')
    //   ->andWhere('h.date <= :endDate')
    //   ->orderBy('h.date', 'ASC')
    // ----------------------------------------------------------------
    public List<Healthentry> findByJournalAndDateRange(int journalId,
                                                        LocalDate startDate,
                                                        LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM healthentry WHERE journal_id=? AND date>=? AND date<=? ORDER BY date ASC";
        List<Healthentry> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, journalId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // findByMonth — Symfony: findByMonth(int $year, int $month)
    // ----------------------------------------------------------------
    public List<Healthentry> findByMonth(int year, int month) throws SQLException {
        String sql = "SELECT * FROM healthentry WHERE YEAR(date)=? AND MONTH(date)=? ORDER BY date ASC";
        List<Healthentry> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // findByDateAndJournal — Symfony: findOneBy(['date'=>$d,'journal'=>$j])
    // Used for duplicate-entry check
    // ----------------------------------------------------------------
    public Optional<Healthentry> findByDateAndJournal(LocalDate date, int journalId) throws SQLException {
        String sql = "SELECT * FROM healthentry WHERE date=? AND journal_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, journalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // ----------------------------------------------------------------
    // search — Symfony: QueryBuilder with LIKE + date filter + sort
    // ----------------------------------------------------------------
    public List<Healthentry> search(String search, LocalDate filterDate,
                                    String sortBy, String sortOrder) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM healthentry WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isEmpty()) {
            sql.append(" AND CAST(poids AS CHAR) LIKE ?");
            params.add("%" + search + "%");
        }
        if (filterDate != null) {
            sql.append(" AND date >= ?");
            params.add(Date.valueOf(filterDate));
        }

        String safeSort  = List.of("date","poids","glycemie","sommeil").contains(sortBy) ? sortBy : "date";
        String safeOrder = "ASC".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(safeSort).append(" ").append(safeOrder);

        List<Healthentry> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ----------------------------------------------------------------
    // save — INSERT or UPDATE + cascade save symptoms
    // Symfony: persist() + flush()  (with cascade: ["persist"])
    // ----------------------------------------------------------------
    public void save(Healthentry entry) throws SQLException {
        if (entry.getId() == 0) {
            insert(entry);
        } else {
            update(entry);
        }
        // Cascade: save all symptoms linked to this entry
        if (entry.getSymptoms() != null) {
            symptomDAO.deleteByEntryId(entry.getId());
            for (var s : entry.getSymptoms()) {
                s.setEntryId(entry.getId());
                symptomDAO.save(s);
            }
        }
    }

    private void insert(Healthentry entry) throws SQLException {
        String sql = "INSERT INTO healthentry (date, poids, glycemie, tension, sommeil, journal_id) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, Date.valueOf(entry.getDate()));
            ps.setDouble(2, entry.getPoids());
            ps.setDouble(3, entry.getGlycemie());
            ps.setString(4, entry.getTension());
            ps.setInt(5, entry.getSommeil());
            ps.setInt(6, entry.getJournalId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) entry.setId(keys.getInt(1));
            }
        }
    }

    private void update(Healthentry entry) throws SQLException {
        String sql = "UPDATE healthentry SET date=?, poids=?, glycemie=?, tension=?, sommeil=?, journal_id=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(entry.getDate()));
            ps.setDouble(2, entry.getPoids());
            ps.setDouble(3, entry.getGlycemie());
            ps.setString(4, entry.getTension());
            ps.setInt(5, entry.getSommeil());
            ps.setInt(6, entry.getJournalId());
            ps.setInt(7, entry.getId());
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // delete — Symfony: remove() + flush()  (with cascade: ["remove"])
    // ----------------------------------------------------------------
    public void delete(int id) throws SQLException {
        symptomDAO.deleteByEntryId(id); // cascade remove symptoms first
        String sql = "DELETE FROM healthentry WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ----------------------------------------------------------------
    // mapRow — Doctrine hydration equivalent
    // ----------------------------------------------------------------
    private Healthentry mapRow(ResultSet rs) throws SQLException {
        return new Healthentry(
            rs.getInt("id"),
            rs.getDate("date").toLocalDate(),
            rs.getDouble("poids"),
            rs.getDouble("glycemie"),
            rs.getString("tension"),
            rs.getInt("sommeil"),
            rs.getInt("journal_id")
        );
    }

    // ----------------------------------------------------------------
    // Analytics methods for Dashboard
    // ----------------------------------------------------------------

    /** Get total count of health entries */
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM healthentry";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /** Get average weight */
    public double getAverageWeight() throws SQLException {
        String sql = "SELECT AVG(poids) FROM healthentry WHERE poids > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double avg = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : avg;
            }
        }
        return 0.0;
    }

    /** Get average sleep hours */
    public double getAverageSleep() throws SQLException {
        String sql = "SELECT AVG(sommeil) FROM healthentry WHERE sommeil > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double avg = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : avg;
            }
        }
        return 0.0;
    }

    /** Get entries count by month for the last N months */
    public List<long[]> getEntriesByMonth(int months) throws SQLException {
        String sql = "SELECT YEAR(date) as year, MONTH(date) as month, COUNT(*) as count " +
            "FROM healthentry " +
            "WHERE date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
            "GROUP BY YEAR(date), MONTH(date) " +
            "ORDER BY year, month";
        List<long[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new long[]{rs.getInt(1), rs.getInt(2), rs.getLong(3)});
                }
            }
        }
        return result;
    }

    /** Get weight trend data (last N entries) */
    public List<double[]> getWeightTrend(int limit) throws SQLException {
        String sql = "SELECT date, poids FROM healthentry WHERE poids > 0 ORDER BY date DESC LIMIT ?";
        List<double[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new double[]{
                        rs.getDate("date").toLocalDate().toEpochDay(),
                        rs.getDouble("poids")
                    });
                }
            }
        }
        return result;
    }

    /** Get glycemia trend data (last N entries) */
    public List<double[]> getGlycemiaTrend(int limit) throws SQLException {
        String sql = "SELECT date, glycemie FROM healthentry WHERE glycemie > 0 ORDER BY date DESC LIMIT ?";
        List<double[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new double[]{
                        rs.getDate("date").toLocalDate().toEpochDay(),
                        rs.getDouble("glycemie")
                    });
                }
            }
        }
        return result;
    }

    /** Get count of entries for a specific journal */
    public int getCountByJournal(int journalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM healthentry WHERE journal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /** Get average weight for a specific journal */
    public double getAverageWeightByJournal(int journalId) throws SQLException {
        String sql = "SELECT AVG(poids) FROM healthentry WHERE journal_id = ? AND poids > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    /** Get average sleep for a specific journal */
    public double getAverageSleepByJournal(int journalId) throws SQLException {
        String sql = "SELECT AVG(sommeil) FROM healthentry WHERE journal_id = ? AND sommeil > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    /** Get average glycemia */
    public double getAverageGlycemia() throws SQLException {
        String sql = "SELECT AVG(glycemie) FROM healthentry WHERE glycemie > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    /** Get average glycemia for a specific journal */
    public double getAverageGlycemiaByJournal(int journalId) throws SQLException {
        String sql = "SELECT AVG(glycemie) FROM healthentry WHERE journal_id = ? AND glycemie > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    /** Get average tension */
    public double getAverageTension() throws SQLException {
        String sql = "SELECT AVG(tension) FROM healthentry WHERE tension > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    /** Get average tension for a specific journal */
    public double getAverageTensionByJournal(int journalId) throws SQLException {
        String sql = "SELECT AVG(tension) FROM healthentry WHERE journal_id = ? AND tension > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    /** Get entries by month for a specific journal */
    public List<long[]> getEntriesByMonthByJournal(int months, int journalId) throws SQLException {
        String sql = "SELECT YEAR(date) as year, MONTH(date) as month, COUNT(*) as count " +
            "FROM healthentry " +
            "WHERE date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) AND journal_id = ? " +
            "GROUP BY YEAR(date), MONTH(date) " +
            "ORDER BY year, month";
        List<long[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months);
            ps.setInt(2, journalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new long[]{rs.getInt(1), rs.getInt(2), rs.getLong(3)});
                }
            }
        }
        return result;
    }

    /** Get weight trend for a specific journal */
    public List<double[]> getWeightTrendByJournal(int limit, int journalId) throws SQLException {
        String sql = "SELECT date, poids FROM healthentry WHERE journal_id = ? AND poids > 0 ORDER BY date DESC LIMIT ?";
        List<double[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new double[]{
                        rs.getDate("date").toLocalDate().toEpochDay(),
                        rs.getDouble("poids")
                    });
                }
            }
        }
        return result;
    }

    /** Get glycemia trend for a specific journal */
    public List<double[]> getGlycemiaTrendByJournal(int limit, int journalId) throws SQLException {
        String sql = "SELECT date, glycemie FROM healthentry WHERE journal_id = ? AND glycemie > 0 ORDER BY date DESC LIMIT ?";
        List<double[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new double[]{
                        rs.getDate("date").toLocalDate().toEpochDay(),
                        rs.getDouble("glycemie")
                    });
                }
            }
        }
        return result;
    }

    /** Get sleep trend data (last N entries) */
    public List<double[]> getSleepTrend(int limit) throws SQLException {
        String sql = "SELECT date, sommeil FROM healthentry WHERE sommeil > 0 ORDER BY date DESC LIMIT ?";
        List<double[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new double[]{
                        rs.getDate("date").toLocalDate().toEpochDay(),
                        rs.getDouble("sommeil")
                    });
                }
            }
        }
        return result;
    }

    /** Get sleep trend for a specific journal */
    public List<double[]> getSleepTrendByJournal(int limit, int journalId) throws SQLException {
        String sql = "SELECT date, sommeil FROM healthentry WHERE journal_id = ? AND sommeil > 0 ORDER BY date DESC LIMIT ?";
        List<double[]> result = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new double[]{
                        rs.getDate("date").toLocalDate().toEpochDay(),
                        rs.getDouble("sommeil")
                    });
                }
            }
        }
        return result;
    }
}
