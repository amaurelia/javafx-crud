# Proyecto Personas (JavaFX + MVC + DAO + MySQL/Oracle)

Este proyecto resuelve el problema de persistir datos en base de datos relacional para una aplicacion de escritorio.

La idea principal es:

- Antes: los datos existian solo en memoria.
- Ahora: las personas se guardan en BD y se conservan al cerrar la app.

## 1. Caso resuelto

La aplicacion administra personas con estos datos:

- `id` (autogenerado en BD)
- `rol` (`Estudiante` o `Profesor`)
- `username`
- `correo`
- `edad`
- `saldo` (calculado en codigo, no se guarda en BD)

Reglas:

- El usuario no puede ser vacio.
- El correo no puede ser vacio y debe contener `@`.
- La edad debe estar entre 0 y 120.
- El rol debe ser `Estudiante` o `Profesor`.

## 2. Estructura y responsabilidades

Se sigue `MVC + DAO`:

- **Conexion**: `src/main/java/com/example/config/ConexionDB.java`
- **Modelo**: `src/main/java/com/example/model/Persona.java`
- **DAO**: `src/main/java/com/example/dao/PersonaDAO.java`
- **Controlador JavaFX**: `src/main/java/com/example/controller/PersonaController.java`
- **Vista FXML**: `src/main/resources/com/example/persona-view.fxml`
- **SQL MySQL**: `personas_mysql.sql`
- **SQL Oracle**: `personas_oracle.sql`

Todos los nombres de clases y archivos ya estan alineados al dominio **Personas**.

## 3. Paso a paso de implementacion

### Paso 1: Configurar dependencias JDBC en Maven

Archivo: `pom.xml`

Asegurate de tener los conectores:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.1.0</version>
</dependency>
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
    <version>23.5.0.24.07</version>
</dependency>
```

Tambien `java.sql` en `module-info.java`:

```java
module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example to javafx.fxml;
    opens com.example.controller to javafx.fxml;
    opens com.example.model to javafx.base;

    exports com.example;
    exports com.example.model;
}
```

### Paso 2: Crear la tabla en base de datos

Para MySQL (Laragon), ejecutar `personas_mysql.sql`:

```sql
CREATE DATABASE IF NOT EXISTS personas;
USE personas;

DROP TABLE IF EXISTS persona;

CREATE TABLE persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rol VARCHAR(40) NOT NULL,
    username VARCHAR(60) NOT NULL UNIQUE,
    correo VARCHAR(120) NOT NULL,
    edad INT NOT NULL,
    CONSTRAINT chk_rol CHECK (rol IN ('Estudiante', 'Profesor')),
    CONSTRAINT chk_correo CHECK (correo LIKE '%@%'),
    CONSTRAINT chk_edad CHECK (edad BETWEEN 0 AND 120)
);
```

El script ya incluye 5 inserts de prueba.

### Paso 3: Centralizar la conexion

Archivo: `src/main/java/com/example/config/ConexionDB.java`

```java
public final class ConexionDB {

    private static final String URL = System.getProperty(
            "personas.db.url",
            "jdbc:mysql://localhost:3306/personas?serverTimezone=UTC");

    private static final String USUARIO = System.getProperty("personas.db.user", "root");
    private static final String PASSWORD = System.getProperty("personas.db.password", "");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
```

Puntos clave:

- Ninguna otra clase guarda usuario/password.
- Puedes cambiar credenciales por propiedades del sistema si lo necesitas.

### Paso 4: Modelo con validaciones y campo calculado

Archivo: `src/main/java/com/example/model/Persona.java`

Conceptos importantes:

- `setRol` acepta solo `Estudiante` o `Profesor`.
- `setCorreo` exige `@`.
- `setEdad` valida rango 0..120.
- `getSaldo()` calcula:
  - Profesor: 200000
  - Estudiante: 80000

Ejemplo de calculo:

```java
public int getSaldo() {
    if (ROL_PROFESOR.equals(rol)) {
        return 200000;
    }
    return 80000;
}
```

### Paso 5: DAO con CRUD y SQL parametrizado

Archivo: `src/main/java/com/example/dao/PersonaDAO.java`

Consultas:

```java
private static final String SQL_SELECT_ALL =
    "SELECT id, rol, username, correo, edad FROM persona ORDER BY id";

private static final String SQL_INSERT =
    "INSERT INTO persona (rol, username, correo, edad) VALUES (?, ?, ?, ?)";

private static final String SQL_UPDATE =
    "UPDATE persona SET rol = ?, username = ?, correo = ?, edad = ? WHERE id = ?";

private static final String SQL_DELETE =
    "DELETE FROM persona WHERE id = ?";
```

Buenas practicas aplicadas:

- Uso de `PreparedStatement`.
- Cierre automatico con `try-with-resources`.
- Manejo de errores con excepcion propia (`DAOException`) para mostrar mensajes amigables en UI.

### Paso 6: Integrar controlador con la BD

Archivo: `src/main/java/com/example/controller/PersonaController.java`

Flujo real en el controlador:

- `initialize()`:
  - Configura tabla.
  - Carga roles en ComboBox.
  - Llama `cargarPersonas()` desde BD.
- `onRegistrar()`:
  - Crea objeto desde formulario.
  - Llama `personaDAO.insertar(...)`.
  - Recarga tabla.
- `onEditar()`:
  - Toma seleccionado.
  - Llama `personaDAO.actualizar(...)`.
  - Recarga tabla.
- `onEliminar()`:
  - Toma seleccionado.
  - Llama `personaDAO.eliminar(id)`.
  - Recarga tabla.

### Paso 7: Vista sin SQL

Archivo: `src/main/resources/com/example/persona-view.fxml`

La vista solo define:

- Campos (`rol`, `usuario`, `correo`, `edad`)
- Botones (`Registrar`, `Editar`, `Eliminar`, `Limpiar`)
- Tabla (`ID`, `Rol`, `Usuario`, `Correo`, `Edad`, `Saldo`)
- Area de mensajes

No contiene SQL ni datos de conexion.

### Paso 8: Arranque de la app

Archivo: `src/main/java/com/example/App.java`

Se carga la vista principal de personas:

```java
scene = new Scene(loadFXML("persona-view"), 980, 620);
stage.setTitle("Gestion de Personas");
```

## 4. Como ejecutar

1. Levanta MySQL (o Oracle) en tu entorno.
2. Ejecuta el script de BD:
   - MySQL: `personas_mysql.sql`
   - Oracle: `personas_oracle.sql`
3. Ajusta credenciales si hace falta en `ConexionDB`.
4. Compila:

```bash
mvn -DskipTests compile
```

5. Ejecuta:

```bash
mvn javafx:run
```

## 5. Pruebas funcionales recomendadas

1. Inicio:
- Abre la app y verifica que se carguen registros desde BD.

2. Registro:
- Agrega una persona nueva y confirma que aparece en la tabla.

3. Edicion:
- Cambia edad/correo de una persona y guarda.

4. Eliminacion:
- Elimina una persona seleccionada.

5. Persistencia:
- Cierra y abre la app. Los cambios deben mantenerse.

6. Validaciones:
- Prueba correo sin `@`.
- Prueba edad fuera de rango.
- Prueba `username` repetido (debe rechazar por restriccion UNIQUE).

## 6. Errores comunes y solucion

- **No conecta a BD**:
  - Revisa puerto, usuario, password y nombre de base.
  - Verifica que MySQL/Oracle este realmente iniciado.

- **Tabla vacia**:
  - Asegurate de ejecutar el script correcto en la base correcta.

- **Error por usuario repetido**:
  - Es esperado por la restriccion `UNIQUE(username)`.

## 7. Resultado esperado

- La aplicacion ya no depende de memoria temporal.
- El CRUD completo funciona contra base de datos.
- La UI se mantiene estable ante errores de BD (mensaje en pantalla, sin caida).
- Se respetan separacion de responsabilidades y buenas practicas DAO.
