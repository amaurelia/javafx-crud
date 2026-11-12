CREATE DATABASE IF NOT EXISTS personas;
USE personas;

DROP TABLE IF EXISTS persona;

CREATE TABLE persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rol VARCHAR(40) NOT NULL,
    username VARCHAR(60) NOT NULL UNIQUE,
    correo VARCHAR(120) NOT NULL,
    edad INT NOT NULL,
    CONSTRAINT chk_rol
        CHECK (rol IN ('Estudiante', 'Profesor')),
    CONSTRAINT chk_correo
        CHECK (correo LIKE '%@%'),
    CONSTRAINT chk_edad
        CHECK (edad BETWEEN 0 AND 120)
);

INSERT INTO persona (rol, username, correo, edad) VALUES
('Profesor', 'jperez', 'jperez@personas.cl', 46),
('Profesor', 'mgonzalez', 'mgonzalez@personas.cl', 54),
('Estudiante', 'csoto', 'csoto@personas.cl', 21),
('Estudiante', 'arojas', 'arojas@personas.cl', 19),
('Profesor', 'lmunoz', 'lmunoz@personas.cl', 38);
