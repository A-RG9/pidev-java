package com.wellora.service;
import java.sql.SQLException;
import java.util.List;
public interface CRUDconsultation<T>{
    void AddConsultation(T t) throws SQLException;
    void DeleteConsultation(int id) throws SQLException;
    List<T> ShowConsultation() throws SQLException;
    void ModifyConsultation(int id, T consultation) throws SQLException;
}
