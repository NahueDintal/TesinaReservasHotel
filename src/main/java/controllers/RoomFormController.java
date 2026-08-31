package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Room;
import repositories.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
  private ListView<String> listFeatures;
  @FXML
  private TextField txtPrice;
  @FXML
  private TextField txtDescription;
  @FXML
  private Button btnSave;
  @FXML
  private Button btnCancel;

  private RoomDAO roomDAO = new RoomDAO();
  private RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
  private RoomViewDAO roomViewDAO = new RoomViewDAO();
  private FeatureDAO featureDAO = new FeatureDAO();

  private Map<Integer, String> roomTypes;
  private Map<Integer, String> roomViews;
  private Map<Integer, String> featuresMap;

  private Room editingRoom;

  @FXML
  public void initialize() {
    loadCatalogs();
    setupActions();
    listFeatures.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  private void loadCatalogs() {
    try {
      roomTypes = roomTypeDAO.listAll();
      comboType.getItems().setAll(roomTypes.values());

      roomViews = roomViewDAO.listAll();
      comboView.getItems().setAll(roomViews.values());

      featuresMap = featureDAO.listAll();
      listFeatures.getItems().setAll(featuresMap.values());
    } catch (SQLException e) {
      showAlert("Error", "No se pudieron cargar los catálogos", e.getMessage());
    }
  }

  private void setupActions() {
    btnCancel.setOnAction(e -> closeWindow());
    btnSave.setOnAction(e -> saveRoom());
  }

  public void setRoom(Room room) {
    this.editingRoom = room;
    lblFormTitle.setText("Editar Habitación");
    txtNumber.setText(String.valueOf(room.getNumber()));
    txtFloor.setText(String.valueOf(room.getFloor()));
    comboType.getSelectionModel().select(room.getTypeName());
    txtCapacity.setText(String.valueOf(room.getCapacity()));
    comboView.getSelectionModel().select(room.getViewName());
    txtPrice.setText(String.valueOf(room.getPrice()));
    txtDescription.setText(room.getDescription());

    // Seleccionar características existentes
    for (String feature : room.getFeatures()) {
      listFeatures.getSelectionModel().select(feature);
    }
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
        showAlert("Éxito", "Habitación guardada", "La habitación se ha guardado correctamente.");
        closeWindow();
      }
    } catch (SQLException e) {
      showAlert("Error", "No se pudo guardar la habitación", e.getMessage());
    }
  }

  private void loadDataFromForm(Room room) {
    room.setNumber(txtNumber.getText().trim());
    room.setFloor(txtFloor.getText().trim());
    room.setIdRoomType(getIdBySelection(comboType, roomTypes));
    room.setCapacity(txtCapacity.getText().trim());
    room.setIdRoomView(getIdBySelection(comboView, roomViews));
    room.setPrice(txtPrice.getText().trim());
    room.setDescription(txtDescription.getText().trim());

    // Obtener características seleccionadas
    List<String> selectedFeatures = new ArrayList<>(listFeatures.getSelectionModel().getSelectedItems());
    room.setFeatures(selectedFeatures);
  }

  private int getIdBySelection(ComboBox<String> combo, Map<Integer, String> map) {
    String selected = combo.getSelectionModel().getSelectedItem();
    for (Map.Entry<Integer, String> entry : map.entrySet()) {
      if (entry.getValue().equals(selected)) {
        return entry.getKey();
      }
    }
    return 0;
  }

  private boolean validateFields() {
    StringBuilder errors = new StringBuilder();

    if (txtNumber.getText().trim().isEmpty())
      errors.append("El número es obligatorio.\n");
    else
      try {
        int n = Integer.parseInt(txtNumber.getText().trim());
        if (n <= 0)
          errors.append("El número debe ser positivo.\n");
      } catch (NumberFormatException e) {
        errors.append("El número debe ser numérico.\n");
      }

    if (txtFloor.getText().trim().isEmpty())
      errors.append("El piso es obligatorio.\n");
    else
      try {
        int f = Integer.parseInt(txtFloor.getText().trim());
        if (f < 0)
          errors.append("El piso no puede ser negativo.\n");
      } catch (NumberFormatException e) {
        errors.append("El piso debe ser numérico.\n");
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
        errors.append("La capacidad debe ser numérica.\n");
      }

    if (comboView.getValue() == null)
      errors.append("Seleccione una vista.\n");
    if (txtPrice.getText().trim().isEmpty())
      errors.append("El precio es obligatorio.\n");
    else
      try {
        double p = Double.parseDouble(txtPrice.getText().trim());
        if (p < 0)
          errors.append("El precio no puede ser negativo.\n");
      } catch (NumberFormatException e) {
        errors.append("El precio debe ser numérico.\n");
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
