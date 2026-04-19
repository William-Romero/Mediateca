# Plan QA - Mediateca UDB (Desafio 2)

## 1. Objetivo

Validar que la aplicacion cumpla comportamiento funcional y tecnico en:

- CRUD por tipo de material.
- Validaciones de negocio implementadas en `MaterialController`.
- Integridad CTI en MySQL.
- Flujo UI principal (`MainWindow` + `DVDTablePanel`).

## 2. Alcance

Tipos cubiertos:

- DVD (`DVD` + 5 digitos)
- Libro (`LIB` + 5 digitos)
- Revista (`REV` + 5 digitos)
- CD (`CDA` + 5 digitos)

No incluido en este plan:

- Pruebas de rendimiento.
- Pruebas de seguridad avanzada.
- Pruebas automatizadas unitarias (el repositorio no incluye suite de tests).

## 3. Ambiente de prueba

- SO: Windows 10/11
- JDK: 21+
- Maven: 3.9+
- MySQL: 8+
- Build base: `mvn clean compile`
- Ejecucion: `mvn exec:java`

Precondiciones:

1. MySQL en ejecucion.
2. Usuario con permisos de creacion sobre `dvd_manager` (si se usa auto-creacion).
3. App iniciada sin error de conexion.

## 4. Datos de referencia sugeridos

Para ejecuciones repetibles usar prefijos de titulo por corrida (ej. `QA_DVD_01`, `QA_LIB_01`).

Ejemplos validos:

- DVD: titulo=`QA_DVD_01`, unidades=`2`, duracion=`120`, genero=`Drama`, director=`Director QA`
- Libro: titulo=`QA_LIB_01`, unidades=`3`, autor=`Autor QA`, isbn=`9789999999991`
- Revista: titulo=`QA_REV_01`, unidades=`4`, periodicidad=`Mensual`, fecha=`2026-04-18`
- CD: titulo=`QA_CDA_01`, unidades=`5`, artista=`Artista QA`, canciones=`10`

## 5. Matriz de casos funcionales

| ID      | Caso                   | Pasos                                                        | Resultado esperado                                        |
| ------- | ---------------------- | ------------------------------------------------------------ | --------------------------------------------------------- |
| QA-F-01 | Alta DVD               | Seleccionar tipo DVD, ingresar campos obligatorios y guardar | Muestra mensaje de creado y codigo con formato `DVD#####` |
| QA-F-02 | Alta Libro             | Seleccionar tipo Libro, completar campos y guardar           | Muestra mensaje de creado y codigo con formato `LIB#####` |
| QA-F-03 | Alta Revista           | Seleccionar tipo Revista, completar campos y guardar         | Muestra mensaje de creado y codigo con formato `REV#####` |
| QA-F-04 | Alta CD                | Seleccionar tipo CD, completar campos y guardar              | Muestra mensaje de creado y codigo con formato `CDA#####` |
| QA-F-05 | Listar por tipo        | Cambiar tipo en combo entre DVD/Libro/Revista/CD             | Tabla muestra columnas y filas del tipo seleccionado      |
| QA-F-06 | Buscar por texto       | En un tipo, usar Buscar por titulo/codigo/campo especifico   | Lista devuelve coincidencias del criterio                 |
| QA-F-07 | Limpiar busqueda       | Ejecutar una busqueda y luego boton Limpiar                  | Campo busqueda queda vacio y se recarga listado completo  |
| QA-F-08 | Refrescar listado      | Usar boton Refrescar                                         | Tabla vuelve a consultar datos y se actualiza             |
| QA-F-09 | Cargar para editar     | Seleccionar fila de tabla                                    | Formulario carga codigo y campos del registro             |
| QA-F-10 | Actualizar registro    | Seleccionar fila, modificar campos editables y guardar       | Cambios persisten, codigo se mantiene                     |
| QA-F-11 | Eliminar registro      | Seleccionar fila y pulsar Eliminar                           | Registro desaparece del listado y de BD                   |
| QA-F-12 | Eliminar sin seleccion | Pulsar Eliminar con formulario limpio                        | Operacion bloqueada con mensaje de error                  |

## 6. Matriz de validaciones de negocio

| ID      | Regla                  | Entrada de prueba                                   | Resultado esperado                                |
| ------- | ---------------------- | --------------------------------------------------- | ------------------------------------------------- |
| QA-V-01 | Campos requeridos      | Dejar vacio un campo obligatorio por tipo y guardar | Bloqueo con mensaje `... es obligatorio`          |
| QA-V-02 | Numericos obligatorios | Dejar vacio `Unidades`, `Duracion` o `Canciones`    | Bloqueo con mensaje de obligatorio                |
| QA-V-03 | Numericos validos      | Ingresar texto en campo numerico                    | Bloqueo con mensaje `... debe ser numerico`       |
| QA-V-04 | Numericos positivos    | Ingresar `0` o negativo en numericos                | Bloqueo con mensaje `... debe ser mayor que cero` |
| QA-V-05 | Fecha revista          | Ingresar fecha distinta a `yyyy-MM-dd`              | Bloqueo con mensaje de formato invalido           |
| QA-V-06 | Max longitud titulo    | Ingresar titulo > 150 caracteres                    | Bloqueo por longitud maxima                       |
| QA-V-07 | Max longitud libro     | Autor > 120 o ISBN > 30                             | Bloqueo por longitud maxima                       |
| QA-V-08 | Max longitud DVD       | Genero > 80 o Director > 120                        | Bloqueo por longitud maxima                       |
| QA-V-09 | Max longitud CD        | Artista > 120                                       | Bloqueo por longitud maxima                       |
| QA-V-10 | Codigo inmutable       | Editar registro existente y guardar                 | Codigo original no cambia                         |

## 7. Cobertura de busqueda por tipo

Campos considerados por DAO:

- DVD: `codigo`, `titulo`, `genero`, `director`
- Libro: `codigo`, `titulo`, `autor`, `isbn`
- Revista: `codigo`, `titulo`, `periodicidad`, `fecha_publicacion (yyyy-MM-dd)`
- CD: `codigo`, `titulo`, `artista`, `numero_canciones`

Validar al menos una busqueda positiva y una negativa por campo relevante.

## 8. Validaciones CTI en base de datos

| ID       | Verificacion SQL        | Resultado esperado                                                |
| -------- | ----------------------- | ----------------------------------------------------------------- |
| QA-DB-01 | Alta de material nuevo  | Existe fila en `material` y en tabla hija correspondiente         |
| QA-DB-02 | Relacion PK/FK          | `tabla_hija.codigo_id = material.codigo`                          |
| QA-DB-03 | Delete cascada          | Al eliminar en app, no quedan huellas en hija                     |
| QA-DB-04 | Restricciones numericas | BD rechaza valores no permitidos si se intenta insercion invalida |

Consultas sugeridas:

```sql
SELECT m.codigo, m.titulo, d.codigo_id
FROM material m
JOIN dvd d ON d.codigo_id = m.codigo
WHERE m.codigo LIKE 'DVD%';

SELECT m.codigo, l.codigo_id
FROM material m
JOIN libro l ON l.codigo_id = m.codigo
WHERE m.codigo LIKE 'LIB%';

SELECT m.codigo, r.codigo_id, r.fecha_publicacion
FROM material m
JOIN revista r ON r.codigo_id = m.codigo
WHERE m.codigo LIKE 'REV%';

SELECT m.codigo, c.codigo_id, c.numero_canciones
FROM material m
JOIN cd c ON c.codigo_id = m.codigo
WHERE m.codigo LIKE 'CDA%';
```

## 9. Criterio de aprobacion

- 100% de casos funcionales QA-F-01..QA-F-12 aprobados.
- 100% de validaciones QA-V-01..QA-V-10 aprobadas.
- 100% de verificaciones de integridad QA-DB-01..QA-DB-04 aprobadas.

## 10. Evidencia minima requerida

- Capturas o video corto de alta, busqueda, actualizacion y eliminacion por tipo.
- Evidencia de mensajes de validacion para casos negativos.
- Evidencia SQL de integridad CTI por cada tipo.
- Registro de fecha/hora, responsable y resultado (Aprobado/Rechazado) por caso.
