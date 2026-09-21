package controllers;

import models.Staff;
import repositories.StaffDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

/**
 * Esta ventana ya NO muestra un log de cambios por empleado.
 * Ahora muestra el listado de TODOS los empleados Inactivos,
 * con la opción de reactivarlos desde acá.
 */
public class StaffHistoryController {

  @FXML
  private TableView<Staff> tableInactive;
  @FXML
  private TableColumn<Staff, String> colName;
  @FXML
  private TableColumn<Staff, String> colDni;
  @FXML
  private TableColumn<Staff, String> colPosition;
  @FXML
  private Button btnReactivate;

  private final StaffDAO staffDAO = new StaffDAO();

  @FXML
  public void initialize() {
    colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
    colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
    colPosition.setCellValueFactory(new PropertyValueFactory<>("positionName"));

    btnReactivate.setOnAction(e -> reactivarSeleccionado());

    cargarInactivos();
  }

  private void cargarInactivos() {
    try {
      tableInactive.setItems(FXCollections.observableArrayList(staffDAO.findInactive()));
    } catch (SQLException e) {
      mostrarAlerta("No se pudo cargar el listado de inactivos: " + e.getMessage());
    }
  }

  private void reactivarSeleccionado() {
    Staff seleccionado = tableInactive.getSelectionModel().getSelectedItem();
    if (seleccionado == null) {
      mostrarAlerta("Seleccioná un empleado de la lista primero.");
      return;
    }

        try {
            staffDAO.reactivate(seleccionado.getId());
            cargarInactivos(); // refresca: el reactivado ya no debería aparecer en la lista
            mostrarAlerta("El personal ha sido reincorporado satisfactoriamente.");
        } catch (SQLException e) {
            mostrarAlerta("No se pudo reactivar: " + e.getMessage());
        }
    }

  private void mostrarAlerta(String mensaje) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
    alert.showAndWait();
  }
}
