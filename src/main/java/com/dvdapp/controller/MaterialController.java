package com.dvdapp.controller;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dvdapp.dao.CDDao;
import com.dvdapp.dao.DVDDao;
import com.dvdapp.dao.LibroDao;
import com.dvdapp.dao.MaterialDao;
import com.dvdapp.dao.RevistaDao;
import com.dvdapp.model.CD;
import com.dvdapp.model.DVD;
import com.dvdapp.model.Libro;
import com.dvdapp.model.Material;
import com.dvdapp.model.Revista;

public class MaterialController {
    private static final Logger LOGGER = LogManager.getLogger(MaterialController.class);
    private static final String TIPO_DVD = "DVD";
    private static final String TIPO_LIBRO = "LIBRO";
    private static final String TIPO_REVISTA = "REVISTA";
    private static final String TIPO_CD = "CD";

    private static final String PREFIJO_DVD = "DVD";
    private static final String PREFIJO_LIBRO = "LIB";
    private static final String PREFIJO_REVISTA = "REV";
    private static final String PREFIJO_CD = "CDA";

    private final DVDDao dvdDao;
    private final LibroDao libroDao;
    private final RevistaDao revistaDao;
    private final CDDao cdDao;

    public MaterialController(
            DVDDao dvdDao,
            LibroDao libroDao,
            RevistaDao revistaDao,
            CDDao cdDao
    ) {
        this.dvdDao = Objects.requireNonNull(dvdDao, "dvdDao es obligatorio.");
        this.libroDao = Objects.requireNonNull(libroDao, "libroDao es obligatorio.");
        this.revistaDao = Objects.requireNonNull(revistaDao, "revistaDao es obligatorio.");
        this.cdDao = Objects.requireNonNull(cdDao, "cdDao es obligatorio.");
    }

    public Material guardarMaterial(Material material) throws SQLException {
        if (material == null) {
            throw new IllegalArgumentException("El material es obligatorio.");
        }

        if (material instanceof DVD dvd) {
            return saveWithGeneratedCode(buildValidatedDvd(dvd, false), PREFIJO_DVD, dvdDao);
        }

        if (material instanceof Libro libro) {
            return saveWithGeneratedCode(buildValidatedLibro(libro, false), PREFIJO_LIBRO, libroDao);
        }

        if (material instanceof Revista revista) {
            return saveWithGeneratedCode(buildValidatedRevista(revista, false), PREFIJO_REVISTA, revistaDao);
        }

        if (material instanceof CD cd) {
            return saveWithGeneratedCode(buildValidatedCd(cd, false), PREFIJO_CD, cdDao);
        }

        throw new IllegalArgumentException("Tipo de material no soportado.");
    }

    public Material actualizarMaterial(Material material) throws SQLException {
        if (material == null) {
            throw new IllegalArgumentException("El material es obligatorio para actualizar.");
        }

        if (material instanceof DVD dvd) {
            DVD validado = buildValidatedDvd(dvd, true);
            dvdDao.update(validado);
            return validado;
        }

        if (material instanceof Libro libro) {
            Libro validado = buildValidatedLibro(libro, true);
            libroDao.update(validado);
            return validado;
        }

        if (material instanceof Revista revista) {
            Revista validada = buildValidatedRevista(revista, true);
            revistaDao.update(validada);
            return validada;
        }

        if (material instanceof CD cd) {
            CD validado = buildValidatedCd(cd, true);
            cdDao.update(validado);
            return validado;
        }

        throw new IllegalArgumentException("Tipo de material no soportado.");
    }

    public void eliminarMaterial(String tipoMaterial, String codigo) throws SQLException {
        String tipo = normalizeTipo(tipoMaterial);
        String codigoLimpio = normalizeCodeRequired(codigo);
        String prefijoEsperado = expectedPrefixForTipo(tipo);
        validateCodePrefix(codigoLimpio, prefijoEsperado);

        Material existente = switch (tipo) {
            case TIPO_DVD -> dvdDao.findByCode(codigoLimpio);
            case TIPO_LIBRO -> libroDao.findByCode(codigoLimpio);
            case TIPO_REVISTA -> revistaDao.findByCode(codigoLimpio);
            case TIPO_CD -> cdDao.findByCode(codigoLimpio);
            default -> throw new IllegalArgumentException("Tipo de material no soportado: " + tipoMaterial);
        };
        if (existente == null) {
            throw new IllegalArgumentException(
                "Debe buscar primero un registro existente antes de eliminar (codigo=" + codigoLimpio + ")."
            );
        }

        switch (Objects.requireNonNull(tipo)) {
            case TIPO_DVD -> dvdDao.deleteByCode(codigoLimpio);
            case TIPO_LIBRO -> libroDao.deleteByCode(codigoLimpio);
            case TIPO_REVISTA -> revistaDao.deleteByCode(codigoLimpio);
            case TIPO_CD -> cdDao.deleteByCode(codigoLimpio);
            default -> throw new IllegalArgumentException("Tipo de material no soportado: " + tipoMaterial);
        }
    }

    public List<Material> listarMateriales(String tipoMaterial) throws SQLException {
        String tipo = normalizeTipo(tipoMaterial);

        return switch (tipo) {
            case TIPO_DVD -> asMaterialList(dvdDao.findAll());
            case TIPO_LIBRO -> asMaterialList(libroDao.findAll());
            case TIPO_REVISTA -> asMaterialList(revistaDao.findAll());
            case TIPO_CD -> asMaterialList(cdDao.findAll());
            default -> throw new IllegalArgumentException("Tipo de material no soportado: " + tipoMaterial);
        };
    }

    public List<Material> buscarMateriales(String tipoMaterial, String texto) throws SQLException {
        String tipo = normalizeTipo(tipoMaterial);
        String query = texto == null ? "" : texto.trim();

        return switch (tipo) {
            case TIPO_DVD -> asMaterialList(query.isEmpty() ? dvdDao.findAll() : dvdDao.search(query));
            case TIPO_LIBRO -> asMaterialList(query.isEmpty() ? libroDao.findAll() : libroDao.search(query));
            case TIPO_REVISTA -> asMaterialList(query.isEmpty() ? revistaDao.findAll() : revistaDao.search(query));
            case TIPO_CD -> asMaterialList(query.isEmpty() ? cdDao.findAll() : cdDao.search(query));
            default -> throw new IllegalArgumentException("Tipo de material no soportado: " + tipoMaterial);
        };
    }

    public Material buscarMaterialPorCodigo(String tipoMaterial, String codigo) throws SQLException {
        String tipo = normalizeTipo(tipoMaterial);
        String codigoLimpio = normalizeCodeRequired(codigo);
        validateCodePrefix(codigoLimpio, expectedPrefixForTipo(tipo));

        return switch (tipo) {
            case TIPO_DVD -> dvdDao.findByCode(codigoLimpio);
            case TIPO_LIBRO -> libroDao.findByCode(codigoLimpio);
            case TIPO_REVISTA -> revistaDao.findByCode(codigoLimpio);
            case TIPO_CD -> cdDao.findByCode(codigoLimpio);
            default -> throw new IllegalArgumentException("Tipo de material no soportado: " + tipoMaterial);
        };
    }

    private DVD buildValidatedDvd(DVD dvd, boolean requireCode) {
        String titulo = validateRequired(dvd.getTitulo(), "Titulo");
        String genero = validateRequired(dvd.getGenero(), "Genero");
        String director = validateRequired(dvd.getDirector(), "Director");

        validateMaxLength(titulo, 150, "Titulo");
        validateMaxLength(genero, 80, "Genero");
        validateMaxLength(director, 120, "Director");

        int duracion = validatePositive(dvd.getDuracion(), "Duracion");

        DVD validated = new DVD();
        if (requireCode) {
            String code = normalizeCodeRequired(dvd.getCodigo());
            validateCodePrefix(code, PREFIJO_DVD);
            validated.setCodigo(code);
        }
        validated.setTitulo(titulo);
        validated.setUnidadesDisponibles(null);
        validated.setDuracion(duracion);
        validated.setGenero(genero);
        validated.setDirector(director);
        return validated;
    }

    private Libro buildValidatedLibro(Libro libro, boolean requireCode) {
        String titulo = validateRequired(libro.getTitulo(), "Titulo");
        String autor = validateRequired(libro.getAutor(), "Autor");
        String editorial = validateRequired(libro.getEditorial(), "Editorial");
        String isbn = validateRequired(libro.getIsbn(), "ISBN");
        int numeroPaginas = validatePositive(libro.getNumeroPaginas(), "NumeroPaginas");
        int anioPublicacion = validatePositive(libro.getAnioPublicacion(), "AnioPublicacion");

        validateMaxLength(titulo, 150, "Titulo");
        validateMaxLength(autor, 120, "Autor");
        validateMaxLength(editorial, 120, "Editorial");
        validateMaxLength(isbn, 30, "ISBN");

        int unidades = validatePositive(libro.getUnidadesDisponibles(), "Unidades disponibles");

        Libro validated = new Libro();
        if (requireCode) {
            String code = normalizeCodeRequired(libro.getCodigo());
            validateCodePrefix(code, PREFIJO_LIBRO);
            validated.setCodigo(code);
        }
        validated.setTitulo(titulo);
        validated.setUnidadesDisponibles(unidades);
        validated.setAutor(autor);
        validated.setNumeroPaginas(numeroPaginas);
        validated.setEditorial(editorial);
        validated.setIsbn(isbn);
        validated.setAnioPublicacion(anioPublicacion);
        return validated;
    }

    private Revista buildValidatedRevista(Revista revista, boolean requireCode) {
        String titulo = validateRequired(revista.getTitulo(), "Titulo");
        String editorial = validateRequired(revista.getEditorial(), "Editorial");
        String periodicidad = validateRequired(revista.getPeriodicidad(), "Periodicidad");
        String fechaPublicacion = validateRequired(revista.getFechaPublicacion(), "FechaPublicacion");

        validateMaxLength(titulo, 150, "Titulo");
        validateMaxLength(editorial, 120, "Editorial");
        validateMaxLength(periodicidad, 50, "Periodicidad");
        validateDateFormat(fechaPublicacion, "FechaPublicacion");

        int unidades = validatePositive(revista.getUnidadesDisponibles(), "Unidades disponibles");

        Revista validated = new Revista();
        if (requireCode) {
            String code = normalizeCodeRequired(revista.getCodigo());
            validateCodePrefix(code, PREFIJO_REVISTA);
            validated.setCodigo(code);
        }
        validated.setTitulo(titulo);
        validated.setUnidadesDisponibles(unidades);
        validated.setEditorial(editorial);
        validated.setPeriodicidad(periodicidad);
        validated.setFechaPublicacion(fechaPublicacion);
        return validated;
    }

    private CD buildValidatedCd(CD cd, boolean requireCode) {
        String titulo = validateRequired(cd.getTitulo(), "Titulo");
        String artista = validateRequired(cd.getArtista(), "Artista");
        String genero = validateRequired(cd.getGenero(), "Genero");

        validateMaxLength(titulo, 150, "Titulo");
        validateMaxLength(artista, 120, "Artista");
        validateMaxLength(genero, 80, "Genero");

        int unidades = validatePositive(cd.getUnidadesDisponibles(), "Unidades disponibles");
        int duracion = validatePositive(cd.getDuracion(), "Duracion");
        int numeroCanciones = validatePositive(cd.getNumeroCanciones(), "NumeroCanciones");

        CD validated = new CD();
        if (requireCode) {
            String code = normalizeCodeRequired(cd.getCodigo());
            validateCodePrefix(code, PREFIJO_CD);
            validated.setCodigo(code);
        }
        validated.setTitulo(titulo);
        validated.setUnidadesDisponibles(unidades);
        validated.setArtista(artista);
        validated.setGenero(genero);
        validated.setDuracion(duracion);
        validated.setNumeroCanciones(numeroCanciones);
        return validated;
    }

    private <T extends Material> T saveWithGeneratedCode(T material, String prefix, MaterialDao<T> dao) throws SQLException {
        for (int attempt = 0; attempt < 5; attempt++) {
            material.setCodigo(generateNextCode(prefix, dao));
            try {
                return dao.save(material);
            } catch (SQLException ex) {
                if (isDuplicateCodeError(ex) && attempt < 4) {
                    LOGGER.warn("Colision de codigo detectada para prefijo {}. Reintentando...", prefix);
                    continue;
                }
                throw ex;
            }
        }
        throw new SQLException("No se pudo generar un codigo unico para el material.");
    }

    private <T extends Material> String generateNextCode(String prefix, MaterialDao<T> dao) throws SQLException {
        String maxCode = dao.findMaxCode(prefix);
        return nextCodeFromMax(prefix, maxCode);
    }

    private String nextCodeFromMax(String prefix, String maxCode) {
        int next = 1;
        if (maxCode != null && !maxCode.isBlank() && maxCode.startsWith(prefix) && maxCode.length() > prefix.length()) {
            String numeric = maxCode.substring(prefix.length());
            try {
                next = Integer.parseInt(numeric) + 1;
            } catch (NumberFormatException ignored) {
                next = 1;
            }
        }
        return prefix + String.format("%05d", next);
    }

    private boolean isDuplicateCodeError(SQLException ex) {
        if (ex == null) {
            return false;
        }
        String sqlState = ex.getSQLState();
        return ex instanceof SQLIntegrityConstraintViolationException
                || "23000".equals(sqlState)
                || ex.getErrorCode() == 1062;
    }

    private String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " es obligatorio.");
        }
        return value.trim();
    }

    private void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " excede el maximo de " + maxLength + " caracteres.");
        }
    }

    private void validateDateFormat(String date, String fieldName) {
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " debe tener formato yyyy-MM-dd.");
        }
    }

    private int validatePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " debe ser mayor que cero.");
        }
        return value;
    }

    private String normalizeTipo(String tipoMaterial) {
        if (tipoMaterial == null || tipoMaterial.isBlank()) {
            throw new IllegalArgumentException("Tipo de material es obligatorio.");
        }
        return tipoMaterial.trim().toUpperCase();
    }

    private String normalizeCodeRequired(String code) {
        String cleanCode = validateRequired(code, "Codigo").toUpperCase();
        if (cleanCode.length() > 20) {
            throw new IllegalArgumentException("Codigo excede el maximo de 20 caracteres.");
        }
        return cleanCode;
    }

    private String expectedPrefixForTipo(String tipo) {
        return switch (tipo) {
            case TIPO_DVD -> PREFIJO_DVD;
            case TIPO_LIBRO -> PREFIJO_LIBRO;
            case TIPO_REVISTA -> PREFIJO_REVISTA;
            case TIPO_CD -> PREFIJO_CD;
            default -> throw new IllegalArgumentException("Tipo de material no soportado: " + tipo);
        };
    }

    private void validateCodePrefix(String code, String prefix) {
        if (!code.startsWith(prefix)) {
            throw new IllegalArgumentException("Codigo invalido para el tipo seleccionado. Debe iniciar con " + prefix + ".");
        }
        if (code.length() != prefix.length() + 5) {
            throw new IllegalArgumentException("Codigo invalido. Debe usar formato " + prefix + " + 5 digitos.");
        }
    }

    private <T extends Material> List<Material> asMaterialList(List<T> source) {
        return new ArrayList<>(source);
    }
}
