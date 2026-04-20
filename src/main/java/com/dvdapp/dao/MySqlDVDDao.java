package com.dvdapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dvdapp.model.DVD;

public class MySqlDVDDao extends AbstractMySqlMaterialDao<DVD> implements DVDDao {
    private static final Logger LOGGER = LogManager.getLogger(MySqlDVDDao.class);
    private static final String CODE_PREFIX = "DVD";
    private static final int CODE_NUMERIC_LENGTH = 5;

    public MySqlDVDDao() throws SQLException {
        super();
        createCtiTablesIfNeeded();
        migrateLegacyDvdsIfPresent();
        LOGGER.info("DAO CTI de DVDs inicializado y estructura material/dvd verificada.");
    }

    @Override
    public DVD save(DVD dvd) throws SQLException {
        final String sqlMaterial = "INSERT INTO material (codigo, titulo, unidades_disponibles) VALUES (?, ?, ?)";
        final String sqlDvd = "INSERT INTO dvd (codigo_id, duracion, genero, director) VALUES (?, ?, ?, ?)";

        String code = normalizeCode(dvd.getCodigo());
        if (code == null || code.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio.");
        }

        try (Connection connection = ConnectionManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                try (PreparedStatement materialStatement = connection.prepareStatement(sqlMaterial);
                     PreparedStatement dvdStatement = connection.prepareStatement(sqlDvd)) {
                    materialStatement.setString(1, code);
                    materialStatement.setString(2, dvd.getTitulo());
                    materialStatement.setObject(3, dvd.getUnidadesDisponibles());
                    materialStatement.executeUpdate();

                    dvdStatement.setString(1, code);
                    dvdStatement.setInt(2, dvd.getDuracion());
                    dvdStatement.setString(3, dvd.getGenero());
                    dvdStatement.setString(4, dvd.getDirector());
                    dvdStatement.executeUpdate();
                }

                connection.commit();
                dvd.setCodigo(code);
                LOGGER.info("DVD guardado con CTI. codigo={}, titulo={}", dvd.getCodigo(), dvd.getTitulo());
                return dvd;
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error guardando DVD CTI (codigo={}): {}", code, ex.getMessage(), ex);
                throw ex;
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        }
    }

    @Override
    public void update(DVD dvd) throws SQLException {
        final String sqlMaterial = "UPDATE material SET titulo = ?, unidades_disponibles = ? WHERE codigo = ?";
        final String sqlDvd = "UPDATE dvd SET duracion = ?, genero = ?, director = ? WHERE codigo_id = ?";

        String code = normalizeCode(dvd.getCodigo());
        if (code == null || code.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para actualizar.");
        }

        try (Connection connection = ConnectionManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                int materialRows;
                try (PreparedStatement materialStatement = connection.prepareStatement(sqlMaterial)) {
                    materialStatement.setString(1, dvd.getTitulo());
                    materialStatement.setObject(2, dvd.getUnidadesDisponibles());
                    materialStatement.setString(3, code);
                    materialRows = materialStatement.executeUpdate();
                }
                if (materialRows == 0) {
                    throw new SQLException("No se encontro el material a actualizar (codigo=" + code + ").");
                }

                int dvdRows;
                try (PreparedStatement dvdStatement = connection.prepareStatement(sqlDvd)) {
                    dvdStatement.setInt(1, dvd.getDuracion());
                    dvdStatement.setString(2, dvd.getGenero());
                    dvdStatement.setString(3, dvd.getDirector());
                    dvdStatement.setString(4, code);
                    dvdRows = dvdStatement.executeUpdate();
                }
                if (dvdRows == 0) {
                    throw new SQLException("No se encontro el detalle DVD a actualizar (codigo=" + code + ").");
                }

                connection.commit();
                LOGGER.info("DVD actualizado con CTI. codigo={}, titulo={}", code, dvd.getTitulo());
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error actualizando DVD CTI (codigo={}): {}", code, ex.getMessage(), ex);
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
            LOGGER.info("DVD eliminado con CTI. codigo={}", cleanCode);
        } catch (SQLException ex) {
            LOGGER.error("Error eliminando DVD CTI (codigo={}): {}", cleanCode, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public DVD findByCode(String code) throws SQLException {
        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, d.duracion, d.genero, d.director "
                + "FROM material m INNER JOIN dvd d ON d.codigo_id = m.codigo WHERE m.codigo = ?";
        String cleanCode = normalizeCode(code);
        if (cleanCode == null || cleanCode.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para consultar.");
        }

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cleanCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LOGGER.info("DVD encontrado por codigo={}", cleanCode);
                    return mapRow(resultSet);
                }
            }
            LOGGER.info("No se encontro DVD para codigo={}", cleanCode);
            return null;
        } catch (SQLException ex) {
            LOGGER.error("Error buscando DVD por codigo={}: {}", cleanCode, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public List<DVD> findAll() throws SQLException {
        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, d.duracion, d.genero, d.director "
                + "FROM material m INNER JOIN dvd d ON d.codigo_id = m.codigo ORDER BY m.codigo DESC";
        List<DVD> dvds = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                dvds.add(mapRow(resultSet));
            }
            LOGGER.info("Listado de DVDs obtenido. total={}", dvds.size());
        } catch (SQLException ex) {
            LOGGER.error("Error listando DVDs: {}", ex.getMessage(), ex);
            throw ex;
        }
        return dvds;
    }

    @Override
    public List<DVD> search(String text) throws SQLException {
        if (text == null || text.isBlank()) {
            return findAll();
        }

        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, d.duracion, d.genero, d.director "
                + "FROM material m INNER JOIN dvd d ON d.codigo_id = m.codigo "
                + "WHERE LOWER(m.codigo) LIKE ? OR LOWER(m.titulo) LIKE ? OR LOWER(d.genero) LIKE ? OR LOWER(d.director) LIKE ? "
                + "ORDER BY m.codigo DESC";

        String criteria = "%" + text.trim().toLowerCase() + "%";
        List<DVD> dvds = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, criteria);
            statement.setString(2, criteria);
            statement.setString(3, criteria);
            statement.setString(4, criteria);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    dvds.add(mapRow(resultSet));
                }
            }
            LOGGER.info("Busqueda de DVDs completada. texto='{}', resultados={}", text, dvds.size());
        } catch (SQLException ex) {
            LOGGER.error("Error buscando DVDs con texto='{}': {}", text, ex.getMessage(), ex);
            throw ex;
        }
        return dvds;
    }

    private void createCtiTablesIfNeeded() throws SQLException {
        final String dvdSql = """
                CREATE TABLE IF NOT EXISTS dvd (
                    codigo_id VARCHAR(20) PRIMARY KEY,
                    duracion INT NOT NULL,
                    genero VARCHAR(80) NOT NULL,
                    director VARCHAR(120) NOT NULL,
                    CONSTRAINT chk_dvd_duracion CHECK (duracion > 0),
                    CONSTRAINT fk_dvd_material FOREIGN KEY (codigo_id)
                        REFERENCES material(codigo)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                )
                """;

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement dvdStatement = connection.prepareStatement(dvdSql)) {
            dvdStatement.execute();

            createIndexIfNeeded(connection, "CREATE INDEX idx_material_titulo ON material (titulo)");
            createIndexIfNeeded(connection, "CREATE INDEX idx_dvd_genero ON dvd (genero)");
            createIndexIfNeeded(connection, "CREATE INDEX idx_dvd_director ON dvd (director)");

            LOGGER.info("Tablas CTI material/dvd verificadas/creadas correctamente.");
        } catch (SQLException ex) {
            LOGGER.error("Error creando/verificando tablas CTI: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private void migrateLegacyDvdsIfPresent() throws SQLException {
        final String selectLegacySql = "SELECT title, genre, duration, director FROM dvds ORDER BY title ASC";
        final String insertMaterialSql = "INSERT INTO material (codigo, titulo, unidades_disponibles) VALUES (?, ?, ?)";
        final String insertDvdSql = "INSERT INTO dvd (codigo_id, duracion, genero, director) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConnectionManager.getConnection()) {
            if (!tableExists(connection, "dvds")) {
                return;
            }
            if (countRows(connection, "material") > 0) {
                return;
            }

            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                int sequence = 1;
                try (PreparedStatement selectStatement = connection.prepareStatement(selectLegacySql);
                     ResultSet resultSet = selectStatement.executeQuery();
                     PreparedStatement materialStatement = connection.prepareStatement(insertMaterialSql);
                     PreparedStatement dvdStatement = connection.prepareStatement(insertDvdSql)) {

                    while (resultSet.next()) {
                        String code = formatCode(sequence++);

                        materialStatement.setString(1, code);
                        materialStatement.setString(2, resultSet.getString("title"));
                        materialStatement.setObject(3, null);
                        materialStatement.executeUpdate();

                        dvdStatement.setString(1, code);
                        dvdStatement.setInt(2, resultSet.getInt("duration"));
                        dvdStatement.setString(3, resultSet.getString("genre"));
                        dvdStatement.setString(4, resultSet.getString("director"));
                        dvdStatement.executeUpdate();
                    }
                }

                connection.commit();
                LOGGER.info("Migracion automatica desde tabla legacy dvds completada.");
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error migrando datos legacy desde dvds: {}", ex.getMessage(), ex);
                throw ex;
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private int countRows(Connection connection, String tableName) throws SQLException {
        final String sql;
        if ("material".equalsIgnoreCase(tableName)) {
            sql = "SELECT COUNT(*) FROM material";
        } else if ("dvd".equalsIgnoreCase(tableName)) {
            sql = "SELECT COUNT(*) FROM dvd";
        } else {
            throw new IllegalArgumentException("Tabla no permitida para conteo: " + tableName);
        }

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private DVD mapRow(ResultSet resultSet) throws SQLException {
        String code = resultSet.getString("codigo");
        DVD dvd = new DVD();
        dvd.setCodigo(code);
        dvd.setTitulo(resultSet.getString("titulo"));
        dvd.setUnidadesDisponibles(resultSet.getObject("unidades_disponibles", Integer.class));
        dvd.setDuracion(resultSet.getInt("duracion"));
        dvd.setGenero(resultSet.getString("genero"));
        dvd.setDirector(resultSet.getString("director"));
        return dvd;
    }

    private String formatCode(int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Secuencia de codigo invalida.");
        }
        return CODE_PREFIX + String.format("%0" + CODE_NUMERIC_LENGTH + "d", sequence);
    }
}
