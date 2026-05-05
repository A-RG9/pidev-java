package com.wellora.javafx.service;
import com.wellora.javafx.model.Ordonnance;
import java.sql.SQLException;
import java.util.List;

public interface CRUDordonnance {
    void AddOrdonnance(Ordonnance ordonnance) throws SQLException;
    void DeleteOrdonnance(int id) throws SQLException;
    List<Ordonnance> ShowOrdonnance() throws SQLException;
    void ModifyOrdonnance(int id, Ordonnance ordonnance) throws SQLException;
}
