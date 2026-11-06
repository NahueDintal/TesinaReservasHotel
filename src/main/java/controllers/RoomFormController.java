package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import models.Room;
import repositories.RoomDAO;
import repositories.RoomTypeDAO;
import repositories.RoomViewDAO;
import utils.Alerts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoomFormController {

  private static final String SAFE_FEATURE = "Caja fuerte";
  private static final int MAX_DESCRIPTION_LENGTH = 100;

  private static final int MAX_DIGITS_NUMBER = 4;
  private static final int MAX_DIGITS_FLOOR = 3;
  private static final int MAX_DIGITS_CAPACITY = 3;

  private static final int MAX_PRICE_INT_DIGITS = 6;
  private static final int MAX_PRICE_DECIMALS = 2;

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
  private Label lblCharCounter;
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

  /**
   * Campos que ya fueron tocados por el usuario (para mostrar feedback visual).
   */
  private final Set<Control> touchedFields = new HashSet<>();

  @FXML
  public void initialize() {
    loadCatalogs();
    setupActions();
    setupNumericFields();
    setupPriceField();
    setupDescriptionLimit();
    setupValidations();
    btnSave.setDisable(true); // 🔴 Botón deshabilitado al abrir
  }

  private void loadCatalogs() {
    try {
      roomTypes = roomTypeDAO.listAll();
      comboType.getItems().setAll(roomTypes.values());

      roomViews = roomViewDAO.listAll();
      comboView.getItems().setAll(roomViews.values());
    } catch (Exception e) {
      logger.error("No se pudieron cargar los catalogos. {}", e.getMessage(), e);
      showAlert("Error", "No se pudieron cargar los catálogos", e.getMessage());
    }
  }

  private void setupActions() {
    btnCancel.setOnAction(e -> closeWindow());
    btnSave.setOnAction(e -> saveRoom());
  }

  // ============== TEXT FORMATTERS (limitan entrada) ==============

  private void setupNumericField(TextField field, int maxDigits) {
    field.setTextFormatter(new TextFormatter<>(change -> {
      String newText = change.getControlNewText();
      if (newText.isEmpty() || newText.matches("\\d{0," + maxDigits + "}")) {
        return change;
      }
      return null;
    }));
  }

  private void setupPriceField() {
    txtPrice.setTextFormatter(new TextFormatter<>(change -> {
      String newText = change.getControlNewText();
      if (newText.isEmpty())
        return change;
      String regex = "\\d{0," + MAX_PRICE_INT_DIGITS + "}(\\.\\d{0," + MAX_PRICE_DECIMALS + "})?";
      if (newText.matches(regex))
        return change;
      return null;
    }));
  }

  private void setupNumericFields() {
    setupNumericField(txtNumber, MAX_DIGITS_NUMBER);
    setupNumericField(txtFloor, MAX_DIGITS_FLOOR);
    setupNumericField(txtCapacity, MAX_DIGITS_CAPACITY);
  }

  private void setupDescriptionLimit() {
    txtDescription.setTextFormatter(new TextFormatter<>(change -> {
      if (change.getControlNewText().length() <= MAX_DESCRIPTION_LENGTH)
        return change;
      return null;
    }));

    txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
      int len = newVal == null ? 0 : newVal.length();
      lblCharCounter.setText(len + "/" + MAX_DESCRIPTION_LENGTH + " caracteres");
      updateCounterStyle(len);
    });

    updateCounterStyle(txtDescription.getText() == null ? 0 : txtDescription.getText().length());
  }

  private void updateCounterStyle(int len) {
    lblCharCounter.getStyleClass().removeAll("char-counter", "char-counter-warning", "char-counter-limit");
    if (len >= MAX_DESCRIPTION_LENGTH) {
      lblCharCounter.getStyleClass().add("char-counter-limit");
    } else if (len >= MAX_DESCRIPTION_LENGTH * 0.8) {
      lblCharCounter.getStyleClass().add("char-counter-warning");
    } else {
      lblCharCounter.getStyleClass().add("char-counter");
    }
  }

  // ============== VALIDACIONES EN VIVO ==============

  private void setupValidations() {
    // Validar al perder el foco (patrón CustomerForm)
    txtNumber.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal) {
        touchedFields.add(txtNumber);
        validateNumber();
      }
    });

    txtFloor.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal) {
        touchedFields.add(txtFloor);
        validateFloor();
      }
    });

    txtCapacity.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal) {
        touchedFields.add(txtCapacity);
        validateCapacity();
      }
    });

    txtPrice.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal) {
        touchedFields.add(txtPrice);
        validatePrice();
      }
    });

    // Combos: al seleccionar, marcar como tocado y actualizar botón
    comboType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null)
        touchedFields.add(comboType);
      updateSaveButtonState();
    });

    comboView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null)
        touchedFields.add(comboView);
      updateSaveButtonState();
    });

    // Mientras escribe: solo actualizar botón (sin feedback visual)
    txtNumber.textProperty().addListener((obs, o, n) -> updateSaveButtonState());
    txtFloor.textProperty().addListener((obs, o, n) -> updateSaveButtonState());
    txtCapacity.textProperty().addListener((obs, o, n) -> updateSaveButtonState());
    txtPrice.textProperty().addListener((obs, o, n) -> updateSaveButtonState());
  }

  // ============== VALIDACIONES INDIVIDUALES ==============

  private boolean validateNumber() {
    String text = txtNumber.getText();
    boolean valid = false;
    String error = null;

    if (text == null || text.trim().isEmpty()) {
      error = "El número de habitación es obligatorio.";
    } else {
      try {
        int n = Integer.parseInt(text.trim());
        if (n < 1) {
          error = "El número debe ser un valor positivo.";
        } else {
          valid = true;
        }
      } catch (NumberFormatException e) {
        error = "El número debe ser un valor numérico entero.";
      }
    }

    setFieldValid(txtNumber, valid, error);
    updateSaveButtonState();
    return valid;
  }

  private boolean validateFloor() {
    String text = txtFloor.getText();
    boolean valid = false;
    String error = null;

    if (text == null || text.trim().isEmpty()) {
      error = "El piso es obligatorio.";
    } else {
      try {
        int f = Integer.parseInt(text.trim());
        if (f < 0) {
          error = "El piso no puede ser un valor negativo.";
        } else {
          valid = true;
        }
      } catch (NumberFormatException e) {
        error = "El piso debe ser un valor numérico entero.";
      }
    }

    setFieldValid(txtFloor, valid, error);
    updateSaveButtonState();
    return valid;
  }

  private boolean validateCapacity() {
    String text = txtCapacity.getText();
    boolean valid = false;
    String error = null;

    if (text == null || text.trim().isEmpty()) {
      error = "La capacidad es obligatoria.";
    } else {
      try {
        int c = Integer.parseInt(text.trim());
        if (c < 1) {
          error = "La capacidad debe ser un valor positivo.";
        } else {
          valid = true;
        }
      } catch (NumberFormatException e) {
        error = "La capacidad debe ser un valor numérico entero.";
      }
    }

    setFieldValid(txtCapacity, valid, error);
    updateSaveButtonState();
    return valid;
  }

  private boolean validatePrice() {
    String text = txtPrice.getText();
    boolean valid = false;
    String error = null;

    if (text == null || text.trim().isEmpty()) {
      error = "El precio es obligatorio.";
    } else {
      try {
        double p = Double.parseDouble(text.trim());
        if (p < 0) {
          error = "El precio no puede ser un valor negativo.";
        } else {
          valid = true;
        }
      } catch (NumberFormatException e) {
        error = "El precio debe ser un valor numérico.";
      }
    }

    setFieldValid(txtPrice, valid, error);
    updateSaveButtonState();
    return valid;
  }

  // ============== HELPERS SILENCIOSOS (para el botón) ==============

  private boolean isValidNumber() {
    String t = txtNumber.getText();
    if (t == null || t.trim().isEmpty())
      return false;
    try {
      return Integer.parseInt(t.trim()) >= 1;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean isValidFloor() {
    String t = txtFloor.getText();
    if (t == null || t.trim().isEmpty())
      return false;
    try {
      return Integer.parseInt(t.trim()) >= 0;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean isValidCapacity() {
    String t = txtCapacity.getText();
    if (t == null || t.trim().isEmpty())
      return false;
    try {
      return Integer.parseInt(t.trim()) >= 1;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean isValidPrice() {
    String t = txtPrice.getText();
    if (t == null || t.trim().isEmpty())
      return false;
    try {
      return Double.parseDouble(t.trim()) >= 0;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean isValidType() {
    return comboType.getSelectionModel().getSelectedItem() != null;
  }

  private boolean isValidView() {
    return comboView.getSelectionModel().getSelectedItem() != null;
  }

  // ============== FEEDBACK VISUAL ==============

  private void setFieldValid(Control field, boolean valid, String errorMessage) {
    // Solo mostrar feedback si el usuario ya tocó el campo
    if (!touchedFields.contains(field))
      return;

    if (valid) {
      field.setStyle("");
      field.setTooltip(null);
    } else {
      field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
      if (errorMessage != null) {
        field.setTooltip(new Tooltip(errorMessage));
      }
    }
  }

  // ============== ESTADO DEL BOTÓN GUARDAR ==============

  private void updateSaveButtonState() {
    boolean allValid = isValidNumber()
        && isValidFloor()
        && isValidCapacity()
        && isValidPrice()
        && isValidType()
        && isValidView();

    btnSave.setDisable(!allValid);
  }

  // ============== EDICIÓN ==============

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

    // Habilitar botón si los datos cargados son válidos
    updateSaveButtonState();
  }

  // ============== GUARDAR ==============

  private void saveRoom() {
    logger.debug("Ejecutando saveRoom");

    // Validación final (defensiva: normalmente el botón está deshabilitado si hay
    // errores)
    if (!validateAllFields()) {
      return;
    }

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

  private boolean validateAllFields() {
    // Marcar todos como tocados (por si el usuario nunca tocó alguno)
    touchedFields.add(txtNumber);
    touchedFields.add(txtFloor);
    touchedFields.add(txtCapacity);
    touchedFields.add(txtPrice);

    boolean numberValid = validateNumber();
    boolean floorValid = validateFloor();
    boolean capacityValid = validateCapacity();
    boolean priceValid = validatePrice();
    boolean typeValid = isValidType();
    boolean viewValid = isValidView();

    return numberValid && floorValid && capacityValid && priceValid && typeValid && viewValid;
  }

  // ============== CARGA DE DATOS AL MODELO ==============

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
    if (selected == null)
      return 0;
    for (Map.Entry<Integer, String> entry : map.entrySet()) {
      if (entry.getValue().equals(selected)) {
        return entry.getKey();
      }
    }
    return 0;
  }

  // ============== VENTANA ==============

  private void closeWindow() {
    Stage stage = (Stage) btnCancel.getScene().getWindow();
    stage.close();
  }

  private void showAlert(String title, String header, String content) {
    Alerts.info(title, header, content);
  }
}
