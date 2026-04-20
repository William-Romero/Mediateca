package com.dvdapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dvdapp.model.Libro;

public class MySqlLibroDao extends AbstractMySqlMaterialDao<Libro> implements LibroDao {
    private static final Logger LOGGER = LogManager.getLogger(MySqlLibroDao.class);
    private static final int DUPLICATE_COLUMN_ERROR = 1060;
    private static final int UNKNOWN_COLUMN_ERROR = 1054;

    public MySqlLibroDao() throws SQLException {
        super();
        createCtiTablesIfNeeded();
        LOGGER.info("DAO CTI de Libros inicializado y estructura material/libro verificada.");
    }

    @Override
    public Libro save(Libro libro) throws SQLException {
        final String sqlMaterial = "INSERT INTO material (codigo, titulo, unidades_disponibles) VALUES (?, ?, ?)";
        final String sqlLibro = "INSERT INTO libro (codigo_id, autor, numero_paginas, editorial, isbn, anio_publicacion) VALUES (?, ?, ?, ?, ?, ?)";

        String code = normalizeCode(libro.getCodigo());
        if (code == null || code.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio.");
        }

        try (Connection connection = ConnectionManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                try (PreparedStatement materialStatement = connection.prepareStatement(sqlMaterial);
                     PreparedStatement libroStatement = connection.prepareStatement(sqlLibro)) {
                    materialStatement.setString(1, code);
                    materialStatement.setString(2, libro.getTitulo());
                    materialStatement.setObject(3, libro.getUnidadesDisponibles());
                    materialStatement.executeUpdate();

                    libroStatement.setString(1, code);
                    libroStatement.setString(2, libro.getAutor());
                    libroStatement.setObject(3, libro.getNumeroPaginas());
                    libroStatement.setString(4, libro.getEditorial());
                    libroStatement.setString(5, libro.getIsbn());
                    libroStatement.setObject(6, libro.getAnioPublicacion());
                    libroStatement.executeUpdate();
                }

                connection.commit();
                libro.setCodigo(code);
                LOGGER.info("Libro guardado con CTI. codigo={}, titulo={}", libro.getCodigo(), libro.getTitulo());
                return libro;
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error guardando Libro CTI (codigo={}): {}", code, ex.getMessage(), ex);
                throw ex;
            } finally {
                restoreAutoCommit(connection, previousAutoCommit);
            }
        }
    }

    @Override
    public void update(Libro libro) throws SQLException {
        final String sqlMaterial = "UPDATE material SET titulo = ?, unidades_disponibles = ? WHERE codigo = ?";
        final String sqlLibro = "UPDATE libro SET autor = ?, numero_paginas = ?, editorial = ?, isbn = ?, anio_publicacion = ? WHERE codigo_id = ?";

        String code = normalizeCode(libro.getCodigo());
        if (code == null || code.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para actualizar.");
        }

        try (Connection connection = ConnectionManager.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                int materialRows;
                try (PreparedStatement materialStatement = connection.prepareStatement(sqlMaterial)) {
                    materialStatement.setString(1, libro.getTitulo());
                    materialStatement.setObject(2, libro.getUnidadesDisponibles());
                    materialStatement.setString(3, code);
                    materialRows = materialStatement.executeUpdate();
                }
                if (materialRows == 0) {
                    throw new SQLException("No se encontro el material a actualizar (codigo=" + code + ").");
                }

                int libroRows;
                try (PreparedStatement libroStatement = connection.prepareStatement(sqlLibro)) {
                    libroStatement.setString(1, libro.getAutor());
                    libroStatement.setObject(2, libro.getNumeroPaginas());
                    libroStatement.setString(3, libro.getEditorial());
                    libroStatement.setString(4, libro.getIsbn());
                    libroStatement.setObject(5, libro.getAnioPublicacion());
                    libroStatement.setString(6, code);
                    libroRows = libroStatement.executeUpdate();
                }
                if (libroRows == 0) {
                    throw new SQLException("No se encontro el detalle Libro a actualizar (codigo=" + code + ").");
                }

                connection.commit();
                LOGGER.info("Libro actualizado con CTI. codigo={}, titulo={}", code, libro.getTitulo());
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                LOGGER.error("Error actualizando Libro CTI (codigo={}): {}", code, ex.getMessage(), ex);
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
            LOGGER.info("Libro eliminado con CTI. codigo={}", cleanCode);
        } catch (SQLException ex) {
            LOGGER.error("Error eliminando Libro CTI (codigo={}): {}", cleanCode, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public Libro findByCode(String code) throws SQLException {
        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, l.autor, l.numero_paginas, l.editorial, l.isbn, l.anio_publicacion "
                + "FROM material m INNER JOIN libro l ON l.codigo_id = m.codigo WHERE m.codigo = ?";
        String cleanCode = normalizeCode(code);
        if (cleanCode == null || cleanCode.isBlank()) {
            throw new SQLException("El codigo del material es obligatorio para consultar.");
        }

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cleanCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    LOGGER.info("Libro encontrado por codigo={}", cleanCode);
                    return mapRow(resultSet);
                }
            }
            LOGGER.info("No se encontro Libro para codigo={}", cleanCode);
            return null;
        } catch (SQLException ex) {
            LOGGER.error("Error buscando Libro por codigo={}: {}", cleanCode, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public List<Libro> findAll() throws SQLException {
        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, l.autor, l.numero_paginas, l.editorial, l.isbn, l.anio_publicacion "
                + "FROM material m INNER JOIN libro l ON l.codigo_id = m.codigo ORDER BY m.codigo DESC";
        List<Libro> libros = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                libros.add(mapRow(resultSet));
            }
            LOGGER.info("Listado de Libros obtenido. total={}", libros.size());
        } catch (SQLException ex) {
            LOGGER.error("Error listando Libros: {}", ex.getMessage(), ex);
            throw ex;
        }
        return libros;
    }

    @Override
    public List<Libro> search(String text) throws SQLException {
        if (text == null || text.isBlank()) {
            return findAll();
        }

        final String sql = "SELECT m.codigo, m.titulo, m.unidades_disponibles, l.autor, l.numero_paginas, l.editorial, l.isbn, l.anio_publicacion "
                + "FROM material m INNER JOIN libro l ON l.codigo_id = m.codigo "
            + "WHERE LOWER(m.codigo) LIKE ? OR LOWER(m.titulo) LIKE ? OR LOWER(l.autor) LIKE ? OR LOWER(l.editorial) LIKE ? OR LOWER(l.isbn) LIKE ? OR CAST(l.numero_paginas AS CHAR) LIKE ? OR CAST(l.anio_publicacion AS CHAR) LIKE ? "
                + "ORDER BY m.codigo DESC";

        String criteria = "%" + text.trim().toLowerCase() + "%";
        List<Libro> libros = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, criteria);
            statement.setString(2, criteria);
            statement.setString(3, criteria);
            statement.setString(4, criteria);
            statement.setString(5, criteria);
            statement.setString(6, criteria);
            statement.setString(7, criteria);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    libros.add(mapRow(resultSet));
                }
            }
            LOGGER.info("Busqueda de Libros completada. texto='{}', resultados={}", text, libros.size());
        } catch (SQLException ex) {
            LOGGER.error("Error buscando Libros con texto='{}': {}", text, ex.getMessage(), ex);
            throw ex;
        }
        return libros;
    }

    private void createCtiTablesIfNeeded() throws SQLException {
        final String libroSql = """
                CREATE TABLE IF NOT EXISTS libro (
                    codigo_id VARCHAR(20) PRIMARY KEY,
                    autor VARCHAR(120) NOT NULL,
                    numero_paginas INT NOT NULL,
                    editorial VARCHAR(120) NOT NULL,
                    isbn VARCHAR(30) NOT NULL,
                    anio_publicacion INT NOT NULL,
                    CONSTRAINT chk_libro_paginas CHECK (numero_paginas > 0),
                    CONSTRAINT chk_libro_anio CHECK (anio_publicacion > 0),
                    CONSTRAINT fk_libro_material FOREIGN KEY (codigo_id)
                        REFERENCES material(codigo)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                )
                """;

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement libroStatement = connection.prepareStatement(libroSql)) {
            libroStatement.execute();
            renameLegacyAnioColumnIfNeeded(connection);
            addColumnIfNeeded(connection, "ALTER TABLE libro ADD COLUMN numero_paginas INT NOT NULL DEFAULT 1");
            addColumnIfNeeded(connection, "ALTER TABLE libro ADD COLUMN editorial VARCHAR(120) NOT NULL DEFAULT 'N/A'");
            addColumnIfNeeded(connection, "ALTER TABLE libro ADD COLUMN anio_publicacion INT NOT NULL DEFAULT 2000");
            createIndexIfNeeded(connection, "CREATE INDEX idx_libro_autor ON libro (autor)");
            createIndexIfNeeded(connection, "CREATE INDEX idx_libro_editorial ON libro (editorial)");
            createIndexIfNeeded(connection, "CREATE INDEX idx_libro_isbn ON libro (isbn)");
            LOGGER.info("Tabla CTI libro verificada/creada correctamente.");
        } catch (SQLException ex) {
            LOGGER.error("Error creando/verificando tabla CTI libro: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private Libro mapRow(ResultSet resultSet) throws SQLException {
        Libro libro = new Libro();
        libro.setCodigo(resultSet.getString("codigo"));
        libro.setTitulo(resultSet.getString("titulo"));
        libro.setUnidadesDisponibles(resultSet.getInt("unidades_disponibles"));
        libro.setAutor(resultSet.getString("autor"));
        libro.setNumeroPaginas(resultSet.getInt("numero_paginas"));
        libro.setEditorial(resultSet.getString("editorial"));
        libro.setIsbn(resultSet.getString("isbn"));
        libro.setAnioPublicacion(resultSet.getInt("anio_publicacion"));
        return libro;
    }

    private void renameLegacyAnioColumnIfNeeded(Connection connection) throws SQLException {
        String legacyColumnName = "a" + (char) 241 + "o_publicacion";
        String renameLegacyColumnSql = "ALTER TABLE libro CHANGE COLUMN `" + legacyColumnName + "` anio_publicacion INT NOT NULL";
        try (PreparedStatement renameStatement = connection.prepareStatement(renameLegacyColumnSql)) {
            renameStatement.execute();
        } catch (SQLException ex) {
            if (ex.getErrorCode() != UNKNOWN_COLUMN_ERROR && ex.getErrorCode() != DUPLICATE_COLUMN_ERROR) {
                throw ex;
            }
        }
    }
}
