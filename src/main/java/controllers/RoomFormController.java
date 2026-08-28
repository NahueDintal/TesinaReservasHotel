package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import models.Room;
import repositories.RoomDAO;

public class RoomFormController {

  @FXML
  private TextField txtNumber;
  @FXML
  private TextField txtFloor;
  @FXML
  private java.awt.TextField txtType;
  @FXML
  private TextField txtCapacity;
  @FXML
  private TextField txtView;
  @FXML
  private TextField txtFeatures;
  @FXML
  private TextField txtPrice;
  @FXML
  private TextField txtDescription;

  private RoomDAO roomDAO = new RoomDAO();
  private DashboardController dashboardController;

  public void setDashboardController(DashboardController dashboardController) {
    this.dashboardController = dashboardController;
  }

  @FXML
  private void handleSave() {
    String errores = validarCampos();
    if (errores != null) {
      mostrarAlertaError("Datos inválidos", errores);
      return;
    }
    try {
      Room room = new Room(
          txtNumber.getText().trim(),
          txtFloor.getText().trim(),
          txtType.getText().trim(),
          txtCapacity.getText().trim(),
          txtView.getText().trim(),
          txtFeatures.getText().trim(),
          txtPrice.getText().trim(),
          txtDescription.getText().trim().toLowerCase());

      roomDAO.insert(room);

      mostrarAlertaInfo("Éxito", "Habitación creada correctamente.");
      if (dashboardController != null) {
        dashboardController.loadView("/views/Room.fxml");
      }

    } catch (Exception e) {
      mostrarAlertaError("Error inesperado", "No se pudo guardar la habitación: " + e.getMessage());
    }
  }

  private void mostrarAlertaError(String titulo, String mensaje) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(mensaje);
    alert.showAndWait();
  }

  private void mostrarAlertaInfo(String titulo, String mensaje) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(mensaje);
    alert.showAndWait();
  }

  private String validarCampos() {
    StringBuilder errores = new StringBuilder();

    if (txtNumber.getText().trim().isEmpty()) {
      errores.append("El número de habitación es obligatorio.\n");
    }
    if (txtFloor.getText().trim().isEmpty()) {
      errores.append("El piso es obligatorio.\n");
    }
    if (txtType.getText().trim().isEmpty()) {
      errores.append("El tipo de habitación es obligatorio.\n");
    }
    if (txtCapacity.getText().trim().isEmpty()) {
      errores.append("La capaidad de la habitación es obligatoria.\n");
    }
    if (txtView.getText().trim().isEmpty()) {
      errores.append("La vista de la habitación es obligatoria.\n");
    }
    if (txtFeatures.getText().trim().isEmpty()) {
      errores.append("Las caracteristicas de habitación es obligatorio.\n");
    }
    if (txtPrice.getText().trim().isEmpty()) {
      errores.append("El precio de la habitación es obligatorio.\n");
    }
    try {
      int capacidad = Integer.parseInt(txtCapacity.getText().trim());
      if (capacidad <= 0) {
        errores.append("La capacidad debe ser un número positivo.\n");
      }
    } catch (NumberFormatException e) {
      errores.append("La capacidad debe ser un número válido.\n");
    }
    try {
      double precio = Double.parseDouble(txtPrice.getText().trim());
      if (precio < 0) {
        errores.append("El precio no puede ser negativo.\n");
      }
    } catch (NumberFormatException e) {
      errores.append("El precio debe ser un número válido.\n");
    }

    return errores.length() == 0 ? null : errores.toString();
  }

  @FXML
  private void handleCancel() {
    if (dashboardController != null) {
      dashboardController.loadView("/views/Room.fxml");
    }
  }
}
