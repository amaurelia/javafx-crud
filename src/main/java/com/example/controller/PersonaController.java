package com.example.controller;

import java.util.List;

import com.example.dao.DAOException;
import com.example.dao.PersonaDAO;
import com.example.model.Persona;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class PersonaController {

    @FXML
    private ComboBox<String> rolCombo;

    @FXML
    private TextField usuarioField;

    @FXML
    private TextField correoField;

    @FXML
    private TextField edadField;

    @FXML
    private TextField filtroEdadField;

    @FXML
    private CheckBox soloEstudiantesCheck;

    @FXML
    private TableView<Persona> personaTable;

    @FXML
    private TableColumn<Persona, Integer> idColumn;

    @FXML
    private TableColumn<Persona, String> rolColumn;

    @FXML
    private TableColumn<Persona, String> usuarioColumn;

    @FXML
    private TableColumn<Persona, String> correoColumn;

    @FXML
    private TableColumn<Persona, Integer> edadColumn;

    @FXML
    private TableColumn<Persona, Integer> saldoColumn;

    @FXML
    private Label mensajeLabel;

    private final PersonaDAO personaDAO = new PersonaDAO();
    private final ObservableList<Persona> personas = FXCollections.observableArrayList();
    private final FilteredList<Persona> personasFiltradas = new FilteredList<>(personas, p -> true);

    @FXML
    private void initialize() {
        configurarTabla();
        rolCombo.setItems(FXCollections.observableArrayList(
            Persona.ROL_ESTUDIANTE,
            Persona.ROL_PROFESOR));

        personaTable.setItems(personasFiltradas);
        personaTable.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                cargarFormulario(actual);
            }
        });

        cargarPersonas();
    }

    @FXML
    private void onRegistrar() {
        try {
            Persona nueva = crearPersonaDesdeFormulario();
            personaDAO.insertar(nueva);
            cargarPersonas();
            limpiarFormulario();
            mostrarMensaje("Persona registrada correctamente.");

        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage());
        } catch (DAOException e) {
            mostrarMensaje("No se pudo registrar: " + e.getMessage());
        }
    }

    @FXML
    private void onEditar() {
        Persona seleccionado = personaTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Selecciona una persona para editar.");
            return;
        }

        try {
            Persona actualizado = crearPersonaDesdeFormulario();
            actualizado.setId(seleccionado.getId());

            personaDAO.actualizar(actualizado);
            cargarPersonas();
            mostrarMensaje("Persona actualizada correctamente.");

        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage());
        } catch (DAOException e) {
            mostrarMensaje("No se pudo actualizar: " + e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Persona seleccionado = personaTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("Selecciona una persona para eliminar.");
            return;
        }

        try {
            personaDAO.eliminar(seleccionado.getId());
            cargarPersonas();
            limpiarFormulario();
            mostrarMensaje("Persona eliminada correctamente.");

        } catch (DAOException e) {
            mostrarMensaje("No se pudo eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void onLimpiar() {
        limpiarFormulario();
        personaTable.getSelectionModel().clearSelection();
        mostrarMensaje("Formulario limpio.");
    }

    @FXML
    private void onFiltrar() {
        Integer edadMinima = null;
        String edadTexto = filtroEdadField.getText();

        if (edadTexto != null && !edadTexto.trim().isEmpty()) {
            try {
                edadMinima = Integer.parseInt(edadTexto.trim());
            } catch (NumberFormatException e) {
                mostrarMensaje("La edad del filtro debe ser un numero entero.");
                return;
            }
        }

        boolean soloEstudiantes = soloEstudiantesCheck.isSelected();

        final Integer edadFiltro = edadMinima;
        final boolean filtrarSoloEstudiantes = soloEstudiantes;

        personasFiltradas.setPredicate(persona -> {
            boolean cumpleEdad = edadFiltro != null && persona.getEdad() > edadFiltro;
            boolean cumpleEstudiante = filtrarSoloEstudiantes && Persona.ROL_ESTUDIANTE.equals(persona.getRol());

            if (edadFiltro != null && filtrarSoloEstudiantes) {
                return cumpleEdad || cumpleEstudiante;
            }
            if (edadFiltro != null) {
                return cumpleEdad;
            }
            if (filtrarSoloEstudiantes) {
                return cumpleEstudiante;
            }
            return true;
        });

        mostrarMensaje("Filtro aplicado. Registros mostrados: " + personasFiltradas.size());
    }

    @FXML
    private void onQuitarFiltro() {
        filtroEdadField.clear();
        soloEstudiantesCheck.setSelected(false);
        personasFiltradas.setPredicate(p -> true);
        mostrarMensaje("Filtro quitado. Registros mostrados: " + personasFiltradas.size());
    }

    private void configurarTabla() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        rolColumn.setCellValueFactory(new PropertyValueFactory<>("rol"));
        usuarioColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        correoColumn.setCellValueFactory(new PropertyValueFactory<>("correo"));
        edadColumn.setCellValueFactory(new PropertyValueFactory<>("edad"));
        saldoColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().getSaldo()));
    }

    private void cargarPersonas() {
        try {
            List<Persona> lista = personaDAO.obtenerTodos();
            personas.setAll(lista);

            if (lista.isEmpty()) {
                mostrarMensaje("No hay personas registradas en la base de datos.");
            } else {
                mostrarMensaje("Personas cargadas: " + lista.size());
            }
        } catch (DAOException e) {
            personas.clear();
            mostrarMensaje("No se pudo cargar la tabla: " + e.getMessage());
        }
    }

    private Persona crearPersonaDesdeFormulario() {
        String rol = rolCombo.getValue();
        String usuario = usuarioField.getText();
        String correo = correoField.getText();
        String edadTexto = edadField.getText();

        if (rol == null) {
            throw new IllegalArgumentException("Debes seleccionar un rol.");
        }

        int edad;
        try {
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La edad debe ser un numero entero.");
        }

        return new Persona(rol, usuario, correo, edad);
    }

    private void cargarFormulario(Persona persona) {
        rolCombo.setValue(persona.getRol());
        usuarioField.setText(persona.getUsername());
        correoField.setText(persona.getCorreo());
        edadField.setText(String.valueOf(persona.getEdad()));
    }

    private void limpiarFormulario() {
        rolCombo.setValue(null);
        usuarioField.clear();
        correoField.clear();
        edadField.clear();
    }

    private void mostrarMensaje(String mensaje) {
        mensajeLabel.setText(mensaje == null ? "" : mensaje);
    }
}
