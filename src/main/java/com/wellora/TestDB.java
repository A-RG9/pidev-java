package com.wellora;

import com.wellora.util.DBConnection;
import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {

        try (Connection conn = DBConnection.getConnection()) {

            if (conn != null) {
                System.out.println("SUCCESS: Connected to database!");
            } else {
                System.out.println("FAILED: Connection is null");
            }

        } catch (Exception e) {
            System.out.println("ERROR:");
            e.printStackTrace();
        }
    }
}