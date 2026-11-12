package com.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionDB {

    private static final String URL = System.getProperty(
            "personas.db.url",
            "jdbc:mysql://localhost:3306/personas?serverTimezone=UTC");

        private static final String USUARIO = System.getProperty("personas.db.user", "root");
        private static final String PASSWORD = System.getProperty("personas.db.password", "");

    private ConexionDB() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
