package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Room;
import repositories.RoomDAO;

import java.sql.SQLException;

public class RoomFormController {

  @FXML
  private Label lblFormTitle;
  @FXML
  private TextField txtNumber;
  @FXML
  private TextField txtFloor;
  @FXML
  private ComboBox<String> comboType;
  @FXML
  private TextField txtCapacity;
  @FXML
  private ComboBox<String> comboView;
  @FXML
  private TextField txtFeatures;
  @FXML
  private TextField txtPrice;
  @FXML
  private TextField txtDescription;
  @FXML
  private Button btnSave;
  @FXML
  private Button btnCancel;

  private RoomDAO roomDAO = new RoomDAO();
  private Room editingRoom; // null si es nueva

  private final ObservableList<String> typeList = FXCollections.observableArrayList(
      "simple", "doble", "suite", "familiar");
  private final ObservableList<String> viewList = FXCollections.observableArrayList(
      "vista al lago", "vista al jardín", "vista a las montañas");

  @FXML
  public void initialize() {
    comboType.setItems(typeList);
    comboView.setItems(viewList);
    setupButtonActions();
  }

  private void setupButtonActions() {
    btnCancel.setOnAction(e -> closeWindow());
    btnSave.setOnAction(e -> saveRoom());
  }

  public void setRoom(Room room) {
    this.editingRoom = room;
    lblFormTitle.setText("Editar Habitación");
    txtNumber.setText(String.valueOf(room.getNumber()));
    txtFloor.setText(String.valueOf(room.getFloor()));
    comboType.getSelectionModel().select(room.getType());
    txtCapacity.setText(String.valueOf(room.getCapacity()));
    comboView.getSelectionModel().select(room.getView());
    txtFeatures.setText(room.getFeatures());
    txtPrice.setText(String.valueOf(room.getPrice()));
    txtDescription.setText(room.getDescription());
  }

  private void saveRoom() {
    if (!validateFields())
      return;

    Room room = editingRoom != null ? editingRoom : new Room();
    loadDataFromForm(room);

    try {
      boolean success;
      if (editingRoom != null) {
        success = roomDAO.update(room);
      } else {
        success = roomDAO.insert(room);
      }

      if (success) {
        showAlert("Éxito", "Habitación guardada",
            editingRoom != null ? "La habitación ha sido actualizada correctamente."
                : "La habitación se ha creado correctamente.");
        closeWindow();
      }
    } catch (RuntimeException e) {
      showAlert("Error", "No se pudo guardar la habitación", e.getMessage());
    }
  }

  private void loadDataFromForm(Room room) {
    room.setNumber(txtNumber.getText().trim());
    room.setFloor(txtFloor.getText().trim());
    room.setType(comboType.getValue());
    room.setCapacity(txtCapacity.getText().trim());
    room.setView(comboView.getValue());
    room.setFeatures(txtFeatures.getText().trim());
    room.setPrice(txtPrice.getText().trim());
    room.setDescription(txtDescription.getText().trim().toLowerCase());
    room.setAvailable(true);
  }

  private boolean validateFields() {
    StringBuilder errors = new StringBuilder();

    if (txtNumber.getText().trim().isEmpty())
      errors.append("El número es obligatorio.\n");
    else
      try {
        int n = Integer.parseInt(txtNumber.getText().trim());
        if (n <= 0)
          errors.append("El número de habitación debe ser positivo.\n");
      } catch (NumberFormatException e) {
        errors.append("El número de habitación debe ser un número entero.\n");
      }

    if (txtFloor.getText().trim().isEmpty())
      errors.append("El piso es obligatorio.\n");
    else
      try {
        int f = Integer.parseInt(txtFloor.getText().trim());
        if (f < 0)
          errors.append("El piso no puede ser negativo.\n");
      } catch (NumberFormatException e) {
        errors.append("El piso debe ser un número entero.\n");
      }

    if (comboType.getValue() == null)
      errors.append("Seleccione un tipo.\n");
    if (txtCapacity.getText().trim().isEmpty())
      errors.append("La capacidad es obligatoria.\n");
    else
      try {
        int c = Integer.parseInt(txtCapacity.getText().trim());
        if (c <= 0)
          errors.append("La capacidad debe ser positiva.\n");
      } catch (NumberFormatException e) {
        errors.append("La capacidad debe ser un número entero.\n");
      }

    if (comboView.getValue() == null)
      errors.append("Seleccione una vista.\n");
    if (txtFeatures.getText().trim().isEmpty())
      errors.append("Las características son obligatorias.\n");
    if (txtPrice.getText().trim().isEmpty())
      errors.append("El precio es obligatorio.\n");
    else
      try {
        double p = Double.parseDouble(txtPrice.getText().trim());
        if (p < 0)
          errors.append("El precio no puede ser negativo.\n");
      } catch (NumberFormatException e) {
        errors.append("El precio debe ser un número entero.\n");
      }

    if (errors.length() > 0) {
      showAlert("Validación", "Corrija los siguientes errores", errors.toString());
      return false;
    }
    return true;
  }

  private void closeWindow() {
    Stage stage = (Stage) btnCancel.getScene().getWindow();
    stage.close();
  }

  private void showAlert(String title, String header, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
