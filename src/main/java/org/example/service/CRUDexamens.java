package org.example.service;
import org.example.entities.Examens;
import java.sql.SQLException;
import java.util.List;

public interface CRUDexamens {
    void AddExamens(Examens examens) throws SQLException;
    void DeleteExamens(int id) throws SQLException;
    List<Examens> ShowExamens() throws SQLException;
    void ModifyExamens(int id, Examens examens) throws SQLException;
}
