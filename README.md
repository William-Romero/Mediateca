# Mediateca UDB - Desafio 2

Aplicacion de escritorio en Java Swing para gestionar materiales de mediateca.
El proyecto implementa arquitectura MVC + DAO, persistencia JDBC con MySQL y modelo CTI (Class Table Inheritance).

## Funcionalidades

- CRUD completo por tipo de material.
- Busqueda por texto desde la tabla.
- Listado por tipo de material.
- Recarga manual del listado.
- Formulario dinamico segun el tipo seleccionado.

Tipos soportados:

- DVD
- Libro
- Revista
- CD

## Atributos por tipo

- Libro: codigo, titulo, autor, numero de paginas, editorial, ISBN, año de publicacion, unidades disponibles.
- Revista: codigo, titulo, editorial, periodicidad, fecha de publicacion, unidades disponibles.
- CD de audio: codigo, titulo, artista, genero, duracion, numero de canciones, unidades disponibles.
- DVD: codigo, titulo, director, duracion, genero.

## Reglas de negocio implementadas

- Generacion automatica de codigo por prefijo y correlativo de 5 digitos:
  - DVD -> `DVD00001`
  - Libro -> `LIB00001`
  - Revista -> `REV00001`
  - CD -> `CDA00001`
- El codigo es obligatorio para actualizar/eliminar y no se modifica en updates.
- No se permiten campos vacios en datos requeridos.
- Campos numericos deben ser mayores que cero.
- Longitudes maximas validadas en el controlador (titulo, autor, artista, etc.).
- Revista exige fecha con formato `yyyy-MM-dd`.
- Eliminacion protegida: no elimina si el registro no existe o no fue seleccionado.

## Arquitectura

### MVC

- Modelo: `Material` (abstracta), `DVD`, `Libro`, `Revista`, `CD`.
- Vista: `MainWindow` + `DVDTablePanel` (tabla reutilizada para todos los tipos).
- Controlador: `MaterialController`.

### DAO + JDBC

- Contrato comun: `MaterialDao<T extends Material>`.
- Implementaciones MySQL por tipo:
  - `MySqlDVDDao`
  - `MySqlLibroDao`
  - `MySqlRevistaDao`
  - `MySqlCDDao`
- Conexion central: `ConnectionManager`.

## Base de datos (CTI)

Estructura:

- Tabla padre: `material`.
- Tablas hijas: `dvd`, `libro`, `revista`, `cd`.

Integridad:

- PK=FK en hijas (`codigo_id` referencia `material.codigo`).
- `ON DELETE CASCADE` y `ON UPDATE CASCADE`.
- Restricciones `NOT NULL` y checks para valores positivos.

### Inicializacion

- Al iniciar la app, `ConnectionManager` valida/crea la base `dvd_manager`.
- Los DAO crean tablas CTI e indices si no existen.
- El script `src/main/resources/schema.sql` es opcional y util para cargar datos semilla.

## Requisitos

- Java 21+
- Maven 3.9+
- MySQL 8+ en ejecucion

## Variables de entorno opcionales

La app permite sobreescribir configuracion JDBC por variables de entorno:

- `DVD_DB_SERVER_URL`
- `DVD_DB_NAME`
- `DVD_DB_URL`
- `DVD_DB_USER`
- `DVD_DB_PASSWORD`

Si no se definen, se usan valores por defecto en `ConnectionManager`.
Recomendacion: definir credenciales por entorno y no depender del password por defecto en produccion.

## Ejecucion

```powershell
mvn clean compile
mvn exec:java
```

Compilacion verificada en el estado actual del repositorio con `mvn -q -DskipTests compile`.

## Logging

- Configuracion en `src/main/resources/log4j.properties`.
- Salida a consola y archivo `logs/dvdapp.log`.

## Estructura principal

```text
src/main/java/com/dvdapp/
  Main.java
  controller/MaterialController.java
  dao/
    ConnectionManager.java
    MaterialDao.java
    AbstractMySqlMaterialDao.java
    MySqlDVDDao.java
    MySqlLibroDao.java
    MySqlRevistaDao.java
    MySqlCDDao.java
  model/
    Material.java
    DVD.java
    Libro.java
    Revista.java
    CD.java
  view/
    MainWindow.java
    DVDTablePanel.java
src/main/resources/
  schema.sql
  log4j.properties
```

## Estado actual

- CRUD operativo para 4 tipos de material.
- Validaciones de negocio centralizadas en `MaterialController`.
- Persistencia CTI en MySQL con DAO especializados.
- Documentacion QA actualizada en `QA_Casos_Prueba_Mediateca.md`.
