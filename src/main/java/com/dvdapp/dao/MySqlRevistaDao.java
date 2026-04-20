package com.dvdapp.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dvdapp.model.Revista;

public class MySqlRevistaDao extends AbstractMySqlMaterialDao<Revista> implements RevistaDao {
    private static final Logger LOGGER = LogManager.getLogger(MySqlRevistaDao.class);

    public MySqlRevistaDao() throws SQLException {
        super();
        createCtiTablesIfNeeded();
        LOGGER.info("DAO CTI de Revistas inicializado y estructura material/revista verificada.");
    }

    @Override
    public Revista save(Revista revista) throws SQLException {
        final String sqlMaterial = "INSERT INTO material (codigo, titulo, unidades_disponibles) VALUES (?, ?, ?)";
        final String sqlRevista = "INSERT INTO revista (codigo_id, editorial, periodicidad, fecha_publicacion) VALUES (?, ?, ?, ?)";

        String code = normalizeCode(revista.getCodigo());
        if (code == null || code.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio.");
        }

        try (Connection connection = ConnectionManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                try (PreparedStatement materialStatement = connection.prepareStatement(sqlMaterial);
                     PreparedStatement revistaStatement = connection.prepareStatement(sqlRevista)) {
                    materialStatement.setString(1, code);
                    materialStatement.setString(2, revista.getTitulo());
                    materialStatement.setObject(3, revista.getUnidadesDisponibles());
                    materialStatement.executeUpdate();

                    revistaStatement.setString(1, code);
                    revistaStatement.setString(2, revista.getEditorial());
                    revistaStatement.setString(3, revista.getPeriodicidad());
                    revistaStatement.setDate(4, parseSqlDate(revista.getFechaPublicacion()));
                    revistaStatement.executeUpdate();
                }

                connection.commit();
                revista.setCodigo(code);
                LOGGER.info("Revista guardada con CTI. codigo={}, titulo={}", revista.getCodigo(), revista.getTitulo());
                return revista;
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error guardando Revista CTI (codigo={}): {}", code, ex.getMessage(), ex);
                throw ex;
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        }
    }

    @Override
    public void update(Revista revista) throws SQLException {
        final String sqlMaterial = "UPDATE material SET titulo = ?, unidades_disponibles = ? WHERE codigo = ?";
        final String sqlRevista = "UPDATE revista SET editorial = ?, periodicidad = ?, fecha_publicacion = ? WHERE codigo_id = ?";

        String code = normalizeCode(revista.getCodigo());
        if (code == null || code.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para actualizar.");
        }

        try (Connection connection = ConnectionManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                int materialRows;
                try (PreparedStatement materialStatement = connection.prepareStatement(sqlMaterial)) {
                    materialStatement.setString(1, revista.getTitulo());
                    materialStatement.setObject(2, revista.getUnidadesDisponibles());
                    materialStatement.setString(3, code);
                    materialRows = materialStatement.executeUpdate();
                }
                if (materialRows == 0) {
                    throw new SQLException("No se encontro el material a actualizar (codigo=" + code + ").");
                }

                int revistaRows;
                try (PreparedStatement revistaStatement = connection.prepareStatement(sqlRevista)) {
                    revistaStatement.setString(1, revista.getEditorial());
                    revistaStatement.setString(2, revista.getPeriodicidad());
                    revistaStatement.setDate(3, parseSqlDate(revista.getFechaPublicacion()));
                    revistaStatement.setString(4, code);
                    revistaRows = revistaStatement.executeUpdate();
                }
                if (revistaRows == 0) {
                    throw new SQLException("No se encontro el detalle Revista a actualizar (codigo=" + code + ").");
                }

                connection.commit();
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error actualizando Revista CTI (codigo={}): {}", code, ex.getMessage(), ex);
                throw ex;
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        }
    }

    @Override
    public void deleteByCode(String code) throws SQLException {
        String cleanCode = normalizeCode(code);
        if (cleanCode == null || cleanCode.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para eliminar.");
        }

        try {
            super.deleteByCode(cleanCode);
        } catch (SQLException ex) {
            LOGGER.error("Error eliminando Revista CTI (codigo={}): {}", cleanCode, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Revista findByCode(String code) throws SQLException {
        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, r.editorial, r.periodicidad, r.fecha_publicacion "
                + "FROM material m INNER JOIN revista r ON r.codigo_id = m.codigo WHERE m.codigo = ?";
        String cleanCode = normalizeCode(code);
        if (cleanCode == null || cleanCode.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para consultar.");
        }

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cleanCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
            return null;
        } catch (SQLException ex) {
            LOGGER.error("Error buscando Revista por codigo={}: {}", cleanCode, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public List<Revista> findAll() throws SQLException {
        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, r.editorial, r.periodicidad, r.fecha_publicacion "
                + "FROM material m INNER JOIN revista r ON r.codigo_id = m.codigo ORDER BY m.codigo DESC";
        List<Revista> revistas = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                revistas.add(mapRow(resultSet));
            }
        } catch (SQLException ex) {
            LOGGER.error("Error listando Revistas: {}", ex.getMessage(), ex);
            throw ex;
        }
        return revistas;
    }

    @Override
    public List<Revista> search(String text) throws SQLException {
        if (text == null || text.isBlank()) {
            return findAll();
        }

        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, r.editorial, r.periodicidad, r.fecha_publicacion "
                + "FROM material m INNER JOIN revista r ON r.codigo_id = m.codigo "
                + "WHERE LOWER(m.codigo) LIKE ? OR LOWER(m.titulo) LIKE ? OR LOWER(r.editorial) LIKE ? OR LOWER(r.periodicidad) LIKE ? "
                + "OR DATE_FORMAT(r.fecha_publicacion, '%Y-%m-%d') LIKE ? ORDER BY m.codigo DESC";

        String criteria = "%" + text.trim().toLowerCase() + "%";
        List<Revista> revistas = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, criteria);
            statement.setString(2, criteria);
            statement.setString(3, criteria);
            statement.setString(4, criteria);
            statement.setString(5, criteria);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    revistas.add(mapRow(resultSet));
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Error buscando Revistas con texto='{}': {}", text, ex.getMessage(), ex);
            throw ex;
        }
        return revistas;
    }

    private void createCtiTablesIfNeeded() throws SQLException {
        final String revistaSql = """
                CREATE TABLE IF NOT EXISTS revista (
                    codigo_id VARCHAR(20) PRIMARY KEY,
                    editorial VARCHAR(120) NOT NULL,
                    periodicidad VARCHAR(50) NOT NULL,
                    fecha_publicacion DATE NOT NULL,
                    CONSTRAINT fk_revista_material FOREIGN KEY (codigo_id)
                        REFERENCES material(codigo)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                )
                """;

            try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement revistaStatement = connection.prepareStatement(revistaSql)) {
            revistaStatement.execute();
            addColumnIfNeeded(connection, "ALTER TABLE revista ADD COLUMN editorial VARCHAR(120) NOT NULL DEFAULT 'N/A'");
            createIndexIfNeeded(connection, "CREATE INDEX idx_revista_editorial ON revista (editorial)");
            createIndexIfNeeded(connection, "CREATE INDEX idx_revista_periodicidad ON revista (periodicidad)");
        } catch (SQLException ex) {
            LOGGER.error("Error creando/verificando tabla CTI revista: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private Revista mapRow(ResultSet resultSet) throws SQLException {
        Revista revista = new Revista();
        revista.setCodigo(resultSet.getString("codigo"));
        revista.setTitulo(resultSet.getString("titulo"));
        revista.setUnidadesDisponibles(resultSet.getInt("unidades_disponibles"));
        revista.setEditorial(resultSet.getString("editorial"));
        revista.setPeriodicidad(resultSet.getString("periodicidad"));
        Date fecha = resultSet.getDate("fecha_publicacion");
        revista.setFechaPublicacion(fecha == null ? "" : fecha.toString());
        return revista;
    }

    private Date parseSqlDate(String dateText) {
        try {
            return Date.valueOf(dateText.trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("FechaPublicacion debe usar formato yyyy-MM-dd.");
        }
    }
}
