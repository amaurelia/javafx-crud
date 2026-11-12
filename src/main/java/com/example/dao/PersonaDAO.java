package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.config.ConexionDB;
import com.example.model.Persona;

public class PersonaDAO {

    private static final String SQL_SELECT_ALL =
        "SELECT id, rol, username, correo, edad FROM persona ORDER BY id";

    private static final String SQL_INSERT =
        "INSERT INTO persona (rol, username, correo, edad) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE persona SET rol = ?, username = ?, correo = ?, edad = ? WHERE id = ?";

    private static final String SQL_DELETE =
        "DELETE FROM persona WHERE id = ?";

    public List<Persona> obtenerTodos() throws DAOException {
        List<Persona> resultado = new ArrayList<>();

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Persona persona = new Persona();
                persona.setId(rs.getInt("id"));
                persona.setRol(rs.getString("rol"));
                persona.setUsername(rs.getString("username"));
                persona.setCorreo(rs.getString("correo"));
                persona.setEdad(rs.getInt("edad"));
                resultado.add(persona);
            }

        } catch (SQLException e) {
            throw new DAOException(construirMensaje("obtener personas", e), e);
        }

        return resultado;
    }

    public Persona insertar(Persona persona) throws DAOException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, persona.getRol());
            ps.setString(2, persona.getUsername());
            ps.setString(3, persona.getCorreo());
            ps.setInt(4, persona.getEdad());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se pudo insertar la persona.", null);
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    persona.setId(keys.getInt(1));
                }
            }

            return persona;

        } catch (SQLException e) {
            throw new DAOException(construirMensaje("insertar persona", e), e);
        }
    }

    public void actualizar(Persona persona) throws DAOException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, persona.getRol());
            ps.setString(2, persona.getUsername());
            ps.setString(3, persona.getCorreo());
            ps.setInt(4, persona.getEdad());
            ps.setInt(5, persona.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontro la persona a actualizar.", null);
            }

        } catch (SQLException e) {
            throw new DAOException(construirMensaje("actualizar persona", e), e);
        }
    }

    public void eliminar(int id) throws DAOException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DAOException("No se encontro la persona a eliminar.", null);
            }

        } catch (SQLException e) {
            throw new DAOException(construirMensaje("eliminar persona", e), e);
        }
    }

    private String construirMensaje(String operacion, SQLException e) {
        return "No se pudo " + operacion + ". Codigo SQL: " + e.getErrorCode() + ", estado: " + e.getSQLState()
                + ". " + e.getMessage();
    }
}
