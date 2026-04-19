-- ========================================
-- SCHEMA MEDIATECA (CTI - Class Table Inheritance)
-- Tipos soportados:
-- DVD, Libro, Revista, CD
-- ========================================

CREATE DATABASE IF NOT EXISTS dvd_manager;
USE dvd_manager;

-- =========================
-- TABLA BASE (PADRE)
-- =========================
CREATE TABLE IF NOT EXISTS material (
    codigo VARCHAR(20) PRIMARY KEY, -- Formato: DVD00001, LIB00001, REV00001, CDA00001
    titulo VARCHAR(150) NOT NULL,
    unidades_disponibles INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_material_unidades CHECK (unidades_disponibles > 0)
);

-- =========================
-- TABLA HIJA: DVD
-- =========================
CREATE TABLE IF NOT EXISTS dvd (
    codigo_id VARCHAR(20) PRIMARY KEY,
    duracion INT NOT NULL,
    genero VARCHAR(80) NOT NULL,
    director VARCHAR(120) NOT NULL,
    CONSTRAINT fk_dvd_material FOREIGN KEY (codigo_id)
        REFERENCES material(codigo)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_dvd_duracion CHECK (duracion > 0)
);

-- =========================
-- TABLA HIJA: LIBRO
-- =========================
CREATE TABLE IF NOT EXISTS libro (
    codigo_id VARCHAR(20) PRIMARY KEY,
    autor VARCHAR(120) NOT NULL,
    isbn VARCHAR(30) NOT NULL,
    CONSTRAINT fk_libro_material FOREIGN KEY (codigo_id)
        REFERENCES material(codigo)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =========================
-- TABLA HIJA: REVISTA
-- =========================
CREATE TABLE IF NOT EXISTS revista (
    codigo_id VARCHAR(20) PRIMARY KEY,
    periodicidad VARCHAR(50) NOT NULL,
    fecha_publicacion DATE NOT NULL,
    CONSTRAINT fk_revista_material FOREIGN KEY (codigo_id)
        REFERENCES material(codigo)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =========================
-- TABLA HIJA: CD
-- =========================
CREATE TABLE IF NOT EXISTS cd (
    codigo_id VARCHAR(20) PRIMARY KEY,
    artista VARCHAR(120) NOT NULL,
    numero_canciones INT NOT NULL,
    CONSTRAINT fk_cd_material FOREIGN KEY (codigo_id)
        REFERENCES material(codigo)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_cd_canciones CHECK (numero_canciones > 0)
);

-- =========================
-- ÍNDICES
-- =========================
CREATE INDEX idx_material_titulo ON material (titulo);

CREATE INDEX idx_dvd_genero ON dvd (genero);
CREATE INDEX idx_dvd_director ON dvd (director);

CREATE INDEX idx_libro_autor ON libro (autor);
CREATE INDEX idx_libro_isbn ON libro (isbn);

CREATE INDEX idx_revista_periodicidad ON revista (periodicidad);

CREATE INDEX idx_cd_artista ON cd (artista);

-- =========================
-- DATOS DE PRUEBA (DVD)
-- =========================
INSERT INTO material (codigo, titulo, unidades_disponibles)
SELECT 'DVD00001', 'El laberinto del fauno', 3
WHERE NOT EXISTS (SELECT 1 FROM material WHERE codigo = 'DVD00001');

INSERT INTO dvd (codigo_id, duracion, genero, director)
SELECT 'DVD00001', 118, 'Drama/Fantasia', 'Guillermo del Toro'
WHERE NOT EXISTS (SELECT 1 FROM dvd WHERE codigo_id = 'DVD00001');


INSERT INTO material (codigo, titulo, unidades_disponibles)
SELECT 'DVD00002', 'La ciudad de Dios', 2
WHERE NOT EXISTS (SELECT 1 FROM material WHERE codigo = 'DVD00002');

INSERT INTO dvd (codigo_id, duracion, genero, director)
SELECT 'DVD00002', 130, 'Crimen/Drama', 'Fernando Meirelles'
WHERE NOT EXISTS (SELECT 1 FROM dvd WHERE codigo_id = 'DVD00002');


INSERT INTO material (codigo, titulo, unidades_disponibles)
SELECT 'DVD00003', 'Amores perros', 4
WHERE NOT EXISTS (SELECT 1 FROM material WHERE codigo = 'DVD00003');

INSERT INTO dvd (codigo_id, duracion, genero, director)
SELECT 'DVD00003', 154, 'Drama/Thriller', 'Alejandro Gonzalez Inarritu'
WHERE NOT EXISTS (SELECT 1 FROM dvd WHERE codigo_id = 'DVD00003');


-- =========================
-- DATOS DE PRUEBA (LIBRO)
-- =========================
INSERT INTO material (codigo, titulo, unidades_disponibles)
SELECT 'LIB00001', 'Cien años de soledad', 5
WHERE NOT EXISTS (SELECT 1 FROM material WHERE codigo = 'LIB00001');

INSERT INTO libro (codigo_id, autor, isbn)
SELECT 'LIB00001', 'Gabriel Garcia Marquez', '9780307474728'
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE codigo_id = 'LIB00001');


-- =========================
-- DATOS DE PRUEBA (REVISTA)
-- =========================
INSERT INTO material (codigo, titulo, unidades_disponibles)
SELECT 'REV00001', 'National Geographic', 6
WHERE NOT EXISTS (SELECT 1 FROM material WHERE codigo = 'REV00001');

INSERT INTO revista (codigo_id, periodicidad, fecha_publicacion)
SELECT 'REV00001', 'Mensual', '2024-01-01'
WHERE NOT EXISTS (SELECT 1 FROM revista WHERE codigo_id = 'REV00001');


-- =========================
-- DATOS DE PRUEBA (CD)
-- =========================
INSERT INTO material (codigo, titulo, unidades_disponibles)
SELECT 'CDA00001', 'Thriller', 3
WHERE NOT EXISTS (SELECT 1 FROM material WHERE codigo = 'CDA00001');

INSERT INTO cd (codigo_id, artista, numero_canciones)
SELECT 'CDA00001', 'Michael Jackson', 9
WHERE NOT EXISTS (SELECT 1 FROM cd WHERE codigo_id = 'CDA00001');
