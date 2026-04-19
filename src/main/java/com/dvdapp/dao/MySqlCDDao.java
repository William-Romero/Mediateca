package com.dvdapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dvdapp.model.CD;

public class MySqlCDDao extends AbstractMySqlMaterialDao<CD> implements CDDao {
    private static final Logger LOGGER = LogManager.getLogger(MySqlCDDao.class);

    public MySqlCDDao() throws SQLException {
        super();
        createCtiTablesIfNeeded();
        LOGGER.info("DAO CTI de CDs inicializado y estructura material/cd verificada.");
    }

    @Override
    public CD save(CD cd) throws SQLException {
        final String sqlMaterial = "INSERT INTO material (codigo, titulo, unidades_disponibles) VALUES (?, ?, ?)";
        final String sqlCd = "INSERT INTO cd (codigo_id, artista, numero_canciones) VALUES (?, ?, ?)";

        String code = normalizeCode(cd.getCodigo());
        if (code == null || code.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio.");
        }

        try (Connection connection = ConnectionManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                try (PreparedStatement materialStatement = connection.prepareStatement(sqlMaterial);
                     PreparedStatement cdStatement = connection.prepareStatement(sqlCd)) {
                    materialStatement.setString(1, code);
                    materialStatement.setString(2, cd.getTitulo());
                    materialStatement.setInt(3, cd.getUnidadesDisponibles());
                    materialStatement.executeUpdate();

                    cdStatement.setString(1, code);
                    cdStatement.setString(2, cd.getArtista());
                    cdStatement.setInt(3, cd.getNumeroCanciones());
                    cdStatement.executeUpdate();
                }

                connection.commit();
                cd.setCodigo(code);
                LOGGER.info("CD guardado con CTI. codigo={}, titulo={}", cd.getCodigo(), cd.getTitulo());
                return cd;
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error guardando CD CTI (codigo={}): {}", code, ex.getMessage(), ex);
                throw ex;
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        }
    }

    @Override
    public void update(CD cd) throws SQLException {
        final String sqlMaterial = "UPDATE material SET titulo = ?, unidades_disponibles = ? WHERE codigo = ?";
        final String sqlCd = "UPDATE cd SET artista = ?, numero_canciones = ? WHERE codigo_id = ?";

        String code = normalizeCode(cd.getCodigo());
        if (code == null || code.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para actualizar.");
        }

        try (Connection connection = ConnectionManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                int materialRows;
                try (PreparedStatement materialStatement = connection.prepareStatement(sqlMaterial)) {
                    materialStatement.setString(1, cd.getTitulo());
                    materialStatement.setInt(2, cd.getUnidadesDisponibles());
                    materialStatement.setString(3, code);
                    materialRows = materialStatement.executeUpdate();
                }
                if (materialRows == 0) {
                    throw new SQLException("No se encontro el material a actualizar (codigo=" + code + ").");
                }

                int cdRows;
                try (PreparedStatement cdStatement = connection.prepareStatement(sqlCd)) {
                    cdStatement.setString(1, cd.getArtista());
                    cdStatement.setInt(2, cd.getNumeroCanciones());
                    cdStatement.setString(3, code);
                    cdRows = cdStatement.executeUpdate();
                }
                if (cdRows == 0) {
                    throw new SQLException("No se encontro el detalle CD a actualizar (codigo=" + code + ").");
                }

                connection.commit();
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error actualizando CD CTI (codigo={}): {}", code, ex.getMessage(), ex);
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
            LOGGER.error("Error eliminando CD CTI (codigo={}): {}", cleanCode, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public CD findByCode(String code) throws SQLException {
        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, c.artista, c.numero_canciones "
                + "FROM material m INNER JOIN cd c ON c.codigo_id = m.codigo WHERE m.codigo = ?";
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
            LOGGER.error("Error buscando CD por codigo={}: {}", cleanCode, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public List<CD> findAll() throws SQLException {
        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, c.artista, c.numero_canciones "
                + "FROM material m INNER JOIN cd c ON c.codigo_id = m.codigo ORDER BY m.codigo DESC";
        List<CD> cds = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                cds.add(mapRow(resultSet));
            }
        } catch (SQLException ex) {
            LOGGER.error("Error listando CDs: {}", ex.getMessage(), ex);
            throw ex;
        }
        return cds;
    }

    @Override
    public List<CD> search(String text) throws SQLException {
        if (text == null || text.isBlank()) {
            return findAll();
        }

        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, c.artista, c.numero_canciones "
                + "FROM material m INNER JOIN cd c ON c.codigo_id = m.codigo "
                + "WHERE LOWER(m.codigo) LIKE ? OR LOWER(m.titulo) LIKE ? OR LOWER(c.artista) LIKE ? "
                + "OR CAST(c.numero_canciones AS CHAR) LIKE ? ORDER BY m.codigo DESC";

        String criteria = "%" + text.trim().toLowerCase() + "%";
        List<CD> cds = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, criteria);
            statement.setString(2, criteria);
            statement.setString(3, criteria);
            statement.setString(4, criteria);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cds.add(mapRow(resultSet));
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Error buscando CDs con texto='{}': {}", text, ex.getMessage(), ex);
            throw ex;
        }
        return cds;
    }

    private void createCtiTablesIfNeeded() throws SQLException {
        final String cdSql = """
                CREATE TABLE IF NOT EXISTS cd (
                    codigo_id VARCHAR(20) PRIMARY KEY,
                    artista VARCHAR(120) NOT NULL,
                    numero_canciones INT NOT NULL,
                    CONSTRAINT chk_cd_canciones CHECK (numero_canciones > 0),
                    CONSTRAINT fk_cd_material FOREIGN KEY (codigo_id)
                        REFERENCES material(codigo)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                )
                """;

            try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement cdStatement = connection.prepareStatement(cdSql)) {
            cdStatement.execute();
            createIndexIfNeeded(connection, "CREATE INDEX idx_cd_artista ON cd (artista)");
        } catch (SQLException ex) {
            LOGGER.error("Error creando/verificando tabla CTI cd: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private CD mapRow(ResultSet resultSet) throws SQLException {
        CD cd = new CD();
        cd.setCodigo(resultSet.getString("codigo"));
        cd.setTitulo(resultSet.getString("titulo"));
        cd.setUnidadesDisponibles(resultSet.getInt("unidades_disponibles"));
        cd.setArtista(resultSet.getString("artista"));
        cd.setNumeroCanciones(resultSet.getInt("numero_canciones"));
        return cd;
    }
}
