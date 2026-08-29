package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import models.Room;
import repositories.RoomDAO;

public class RoomModifController {

  private final ObservableList<String> typeRoomList = FXCollections.observableArrayList(
      "simple", "doble", "suite", "familiar");
  private final ObservableList<String> viewRoomList = FXCollections.observableArrayList(
      "vista al lago", "vista al jardín", "vista a las montañas");

  @FXML
  private TextField txtNumber;
  @FXML
  private TextField txtFloor;
  @FXML
  private ChoiceBox<String> chbType;
  @FXML
  private TextField txtCapacity;
  @FXML
  private ChoiceBox<String> chbView;
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
  public void initialize() {
    chbType.setItems(typeRoomList);
    chbView.setItems(viewRoomList);
  }

  @FXML
  private void handleSave() {
    String errores = validarCampos();
    if (errores != null) {
      mostrarAlertaError("Datos inválidos", errores);
      return;
    }
    try {
      String type = chbType.getValue() != null ? chbType.getValue().trim() : "";
      String view = chbView.getValue() != null ? chbView.getValue().trim() : "";

      Room room = new Room(
          txtNumber.getText().trim(),
          txtFloor.getText().trim(),
          type,
          txtCapacity.getText().trim(),
          view,
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
    if (chbType.getValue() == null || chbType.getValue().trim().isEmpty()) {
      errores.append("El tipo de habitación es obligatorio.\n");
    }
    if (txtCapacity.getText().trim().isEmpty()) {
      errores.append("La capacidad de la habitación es obligatoria.\n");
    }
    if (chbView.getValue() == null || chbView.getValue().trim().isEmpty()) {
      errores.append("La vista es obligatoria.\n");
    }
    if (txtFeatures.getText().trim().isEmpty()) {
      errores.append("Las características de habitación son obligatorias.\n");
    }
    if (txtPrice.getText().trim().isEmpty()) {
      errores.append("El precio de la habitación es obligatorio.\n");
    }
    // Validación de capacidad numérica y positiva
    try {
      int capacidad = Integer.parseInt(txtCapacity.getText().trim());
      if (capacidad <= 0) {
        errores.append("La capacidad debe ser un número positivo.\n");
      }
    } catch (NumberFormatException e) {
      errores.append("La capacidad debe ser un número válido.\n");
    }
    // Validación de precio numérico y no negativo
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
