package com.dvdapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dvdapp.model.Material;

public abstract class AbstractMySqlMaterialDao<T extends Material> implements MaterialDao<T> {
    private static final int INDEX_ALREADY_EXISTS_ERROR = 1061;
    private static final int DUPLICATE_COLUMN_ERROR = 1060;
    private static volatile boolean baseSchemaChecked;

    protected AbstractMySqlMaterialDao() throws SQLException {
        ensureBaseSchemaIfNeeded();
    }

    @Override
    public void deleteByCode(String code) throws SQLException {
        final String sql = "DELETE FROM material WHERE codigo = ?";
        String cleanCode = normalizeCode(code);
        if (cleanCode == null || cleanCode.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para eliminar.");
        }

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cleanCode);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se encontro el material a eliminar (codigo=" + cleanCode + ").");
            }
        }
    }

    @Override
    public String findMaxCode(String prefix) throws SQLException {
        final String sql = "SELECT codigo FROM material WHERE codigo LIKE ? ORDER BY codigo DESC LIMIT 1";
        String cleanPrefix = prefix == null ? "" : prefix.trim().toUpperCase();

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cleanPrefix + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("codigo");
                }
            }
            return null;
        }
    }

    protected Connection openConnection() throws SQLException {
        return ConnectionManager.getConnection();
    }

    protected String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    protected void rollbackQuietly(Connection connection, SQLException original) throws SQLException {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            throw original;
        }
    }

    protected void restoreAutoCommit(Connection connection, boolean previousAutoCommit) throws SQLException {
        connection.setAutoCommit(previousAutoCommit);
    }

    protected void createIndexIfNeeded(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            if (ex.getErrorCode() != INDEX_ALREADY_EXISTS_ERROR) {
                throw ex;
            }
        }
    }

    protected void addColumnIfNeeded(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException ex) {
            if (ex.getErrorCode() != DUPLICATE_COLUMN_ERROR) {
                throw ex;
            }
        }
    }

    private void ensureBaseSchemaIfNeeded() throws SQLException {
        if (baseSchemaChecked) {
            return;
        }

        synchronized (AbstractMySqlMaterialDao.class) {
            if (baseSchemaChecked) {
                return;
            }

            final String materialSql = """
                    CREATE TABLE IF NOT EXISTS material (
                        codigo VARCHAR(20) PRIMARY KEY,
                        titulo VARCHAR(150) NOT NULL,
                        unidades_disponibles INT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT chk_material_unidades CHECK (unidades_disponibles IS NULL OR unidades_disponibles > 0)
                    )
                    """;

            try (Connection connection = openConnection();
                 PreparedStatement materialStatement = connection.prepareStatement(materialSql)) {
                materialStatement.execute();
                try (PreparedStatement alterStatement = connection.prepareStatement(
                        "ALTER TABLE material MODIFY unidades_disponibles INT NULL"
                )) {
                    alterStatement.execute();
                }
                createIndexIfNeeded(connection, "CREATE INDEX idx_material_titulo ON material (titulo)");
                baseSchemaChecked = true;
            }
        }
    }
}
