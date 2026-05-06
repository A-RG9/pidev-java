package com.wellora.javafx.service;
import com.wellora.javafx.model.Examens;
import java.sql.SQLException;
import java.util.List;

public interface CRUDexamens {
    void AddExamens(Examens examens) throws SQLException;
    void DeleteExamens(int id) throws SQLException;
    List<Examens> ShowExamens() throws SQLException;
    void ModifyExamens(int id, Examens examens) throws SQLException;
}
