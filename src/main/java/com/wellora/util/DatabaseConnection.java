package com.wellora.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

        final String USERNAME = "root";
        final String URL = "jdbc:mysql://localhost:3306/wellora";
        final String PASSWORD ="";

        Connection connection;
        static DatabaseConnection instance;


        // constructeur
        private DatabaseConnection() {
            try {
                connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);
                System.out.println("Connection established");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }


        }

        public static DatabaseConnection getInstance() {
            if (instance == null){
                instance = new DatabaseConnection();
            }
            return instance;
        }

        public Connection getConnection() {
            return connection;
        }
    }

