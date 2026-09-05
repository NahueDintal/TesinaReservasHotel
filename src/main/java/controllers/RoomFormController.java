package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Room;
import repositories.RoomDAO;
import repositories.RoomTypeDAO;
import repositories.RoomViewDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomFormController {

  private static final String SAFE_FEATURE = "Caja fuerte";

  private static final Logger logger = LoggerFactory.getLogger(RoomFormController.class);

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
  private TextField txtPrice;
  @FXML
  private TextField txtDescription;
  @FXML
  private CheckBox chkWifi;
  @FXML
  private CheckBox chkTv;
  @FXML
  private CheckBox chkAc;
  @FXML
  private CheckBox chkMiniBar;
  @FXML
  private Button btnSave;
  @FXML
  private Button btnCancel;

  private final RoomDAO roomDAO = new RoomDAO();
  private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
  private final RoomViewDAO roomViewDAO = new RoomViewDAO();

  private Map<Integer, String> roomTypes;
  private Map<Integer, String> roomViews;

  private Room editingRoom;

  @FXML
  public void initialize() {
    loadCatalogs();
    setupActions();
  }

  private void loadCatalogs() {
    try {
      roomTypes = roomTypeDAO.listAll();
      comboType.getItems().setAll(roomTypes.values());

      roomViews = roomViewDAO.listAll();
      comboView.getItems().setAll(roomViews.values());
    } catch (SQLException e) {
      logger.error("No se pudieron cargar los catalogos. {}", e.getMessage());
      showAlert("Error", "No se pudieron cargar los catálogos", e.getMessage());
    }
  }

  private void setupActions() {
    btnCancel.setOnAction(e -> closeWindow());
    btnSave.setOnAction(e -> saveRoom());
  }

  public void setRoom(Room room) {
    logger.debug("Ejecutando setRoom para room {}", room);
    this.editingRoom = room;
    lblFormTitle.setText("Editar Habitación");
    txtNumber.setText(String.valueOf(room.getNumber()));
    txtFloor.setText(String.valueOf(room.getFloor()));
    comboType.getSelectionModel().select(room.getTypeName());
    txtCapacity.setText(String.valueOf(room.getCapacity()));
    comboView.getSelectionModel().select(room.getViewName());
    txtPrice.setText(String.valueOf(room.getPrice()));
    txtDescription.setText(room.getDescription());

    chkWifi.setSelected(room.getFeatures().contains("WiFi"));
    chkTv.setSelected(room.getFeatures().contains("TV"));
    chkAc.setSelected(room.getFeatures().contains("Aire acondicionado"));
    chkMiniBar.setSelected(room.getFeatures().contains("Minibar"));
  }

  private void saveRoom() {
    logger.debug("Ejecutando saveRoom");
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
    } catch (IllegalArgumentException e) {
      logger.error("Numero de habitación duplicado");
      showAlert("Error", "Número de habitación duplicado", e.getMessage());
    } catch (RuntimeException e) {
      logger.error("No se puede guardar la habitación");
      showAlert("Error", "No se pudo guardar la habitación", e.getMessage());
    }
  }

  private void loadDataFromForm(Room room) {
    room.setNumber(Integer.parseInt(txtNumber.getText().trim()));
    room.setFloor(Integer.parseInt(txtFloor.getText().trim()));
    room.setIdRoomType(getIdBySelection(comboType, roomTypes));
    room.setCapacity(Integer.parseInt(txtCapacity.getText().trim()));
    room.setIdRoomView(getIdBySelection(comboView, roomViews));
    room.setPrice(Double.parseDouble(txtPrice.getText().trim()));
    room.setDescription(txtDescription.getText().trim());

    List<String> selectedFeatures = new ArrayList<>();
    if (chkWifi.isSelected())
      selectedFeatures.add("WiFi");
    if (chkTv.isSelected())
      selectedFeatures.add("TV");
    if (chkAc.isSelected())
      selectedFeatures.add("Aire acondicionado");
    if (chkMiniBar.isSelected())
      selectedFeatures.add("Minibar");

    if (!selectedFeatures.contains(SAFE_FEATURE)) {
      selectedFeatures.add(SAFE_FEATURE);
    }

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

    if (txtNumber.getText().trim().isEmpty()) {
      logger.warn("Intento de no insertar numero de habitación");
      errors.append("El número de habitación es obligatorio.\n");
    } else {
      try {
        int n = Integer.parseInt(txtNumber.getText().trim());
        if (n < 0) {
          logger.warn("Intento de insertar valor '{}' menor que 1", txtNumber.getText());
          errors.append("El número debe ser positivo.\n");
        }
        if (n > 9999) {
          logger.warn("Intento de insertar valor '{}' mayor que 9999", txtNumber.getText());
          errors.append("El número no puede ser mayor a 9999.\n");
        }
      } catch (NumberFormatException e) {
        logger.error("Ingreso de valor '{}' que no es un numero", txtNumber.getText());
        errors.append("El número debe ser un entero.\n");
      }
    }

    if (txtFloor.getText().trim().isEmpty()) {
      logger.warn("Intento de no insertar piso");
      errors.append("El piso es obligatorio.\n");
    } else {
      try {
        int f = Integer.parseInt(txtFloor.getText().trim());
        if (f < 0) {
          logger.warn("Intento de insertar valor '{}' menor que 1", txtFloor.getText());
          errors.append("El piso no puede ser negativo.\n");
        }
        if (f > 999) {
          logger.warn("Intento de insertar valor '{}' mayor que 999", txtFloor.getText());
          errors.append("El piso no puede ser mayor a 999.\n");
        }
      } catch (NumberFormatException e) {
        logger.error("Ingreso de valor '{}' que no es un número", txtFloor.getText());
        errors.append("El piso debe ser un número entero.\n");
      }
    }

    if (comboType.getValue() == null) {
      logger.warn("Intento de no insertar ningun tipo de habitación");
      errors.append("Seleccione un tipo de habitación.\n");
    }

    if (txtCapacity.getText().trim().isEmpty()) {
      logger.warn("Intento de no insertar capacidad");
      errors.append("La capacidad es obligatoria.\n");
    } else {
      try {
        int c = Integer.parseInt(txtCapacity.getText().trim());
        if (c < 0) {
          logger.warn("Intento de insertar valor '{}' menor que 1", txtCapacity.getText());
          errors.append("La capacidad debe ser positiva.\n");
        }
        if (c > 999) {
          logger.warn("Intento de insertar valor '{}' mayor que 999", txtCapacity.getText());
          errors.append("La capacidad no puede ser mayor a 999.\n");
        }
      } catch (NumberFormatException e) {
        logger.error("Intento de insertar valor '{}' que no es un número", txtCapacity.getText());
        errors.append("La capacidad debe ser un número entero.\n");
      }
    }

    if (comboView.getValue() == null) {
      logger.warn("Intento de no insertar vista");
      errors.append("Seleccione una vista.\n");
    }

    if (txtPrice.getText().trim().isEmpty()) {
      logger.warn("Intento de no insertar precio");
      errors.append("El precio es obligatorio.\n");
    } else {
      try {
        double p = Double.parseDouble(txtPrice.getText().trim());
        if (p < 0)
          logger.warn("Intento de insertar valor '{}' menor que 1", txtPrice.getText());
        errors.append("El precio no puede ser negativo.\n");
      } catch (NumberFormatException e) {
        errors.append("El precio debe ser un número (puede ser decimal).\n");
        logger.error("Intento de insertar valor '{}' que no es un número", txtPrice.getText());
      }
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
