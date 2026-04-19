package com.dvdapp.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ConnectionManager {

    private static final Logger LOGGER = LogManager.getLogger(ConnectionManager.class);

    private static final String DEFAULT_DB_NAME = "dvd_manager";
    private static final String DEFAULT_SERVER_URL =
        "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "Rome2026#";

    private static final String DB_NAME = readEnv("DVD_DB_NAME", DEFAULT_DB_NAME);
    private static final String SERVER_URL = readEnv("DVD_DB_SERVER_URL", DEFAULT_SERVER_URL);
    private static final String URL = readEnv(
        "DVD_DB_URL",
        "jdbc:mysql://localhost:3306/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );
    private static final String USER = readEnv("DVD_DB_USER", DEFAULT_USER);
    private static final String PASSWORD = readEnv("DVD_DB_PASSWORD", DEFAULT_PASSWORD);

    private static volatile boolean databaseChecked;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("MySQL JDBC driver not found.", ex);
        }
    }

    private ConnectionManager() {
    }

    public static Connection getConnection() throws SQLException {
        ensureDatabaseExists();
        try {
            Connection connection = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );
            LOGGER.info("Conexion JDBC establecida correctamente hacia {}", URL);
            return connection;
        } catch (SQLException ex) {
            LOGGER.error("Error al abrir conexion JDBC hacia {}: {}", URL, ex.getMessage(), ex);
            throw ex;
        }
    }

    private static void ensureDatabaseExists() throws SQLException {
        if (databaseChecked) {
            return;
        }

        synchronized (ConnectionManager.class) {
            if (databaseChecked) {
                return;
            }

            String sql = "CREATE DATABASE IF NOT EXISTS `" + DB_NAME + "`";

            try (Connection connection = DriverManager.getConnection(
                    SERVER_URL,
                    USER,
                    PASSWORD
            );
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.execute();
                databaseChecked = true;

                LOGGER.info("Base de datos verificada/inicializada: {}", DB_NAME);

            } catch (SQLException ex) {
                LOGGER.error("Error verificando/creando la base de datos {}: {}", DB_NAME, ex.getMessage(), ex);
                throw ex;
            }
        }
    }

    private static String readEnv(String envName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}