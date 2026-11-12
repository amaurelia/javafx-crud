package com.example.model;

public class Persona {

    public static final String ROL_ESTUDIANTE = "Estudiante";
    public static final String ROL_PROFESOR = "Profesor";

    private int id;
    private String rol;
    private String username;
    private String correo;
    private int edad;

    public Persona() {
        this.id = 0;
    }

    public Persona(String rol, String username, String correo, int edad) {
        this.id = 0;
        setRol(rol);
        setUsername(username);
        setCorreo(correo);
        setEdad(edad);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id no puede ser negativo.");
        }
        this.id = id;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        if (!ROL_ESTUDIANTE.equals(rol) && !ROL_PROFESOR.equals(rol)) {
            throw new IllegalArgumentException("El rol debe ser Estudiante o Profesor.");
        }
        this.rol = rol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario no puede ser nulo ni vacio.");
        }
        this.username = username.trim();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo no puede ser vacio.");
        }
        if (!correo.contains("@")) {
            throw new IllegalArgumentException("El correo debe contener '@'.");
        }
        this.correo = correo.trim();
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        if (edad < 0 || edad > 120) {
            throw new IllegalArgumentException("La edad debe estar entre 0 y 120.");
        }
        this.edad = edad;
    }

    public int getSaldo() {
        if (ROL_PROFESOR.equals(rol)) {
            return 200000;
        }
        return 80000;
    }
}
