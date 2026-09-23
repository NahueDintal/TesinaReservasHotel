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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomFormController {

  private static final String SAFE_FEATURE = "Caja fuerte";
  private static final int MAX_DESCRIPTION_LENGTH = 100;

  // Límites de dígitos por campo
  private static final int MAX_DIGITS_NUMBER = 4; // 0..9999
  private static final int MAX_DIGITS_FLOOR = 3; // 0..999
  private static final int MAX_DIGITS_CAPACITY = 3; // 0..999

  // Límite de precio (para el TextFormatter)
  private static final int MAX_PRICE_INT_DIGITS = 6; // hasta 999999
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

  // ============================================================
  // INITIALIZATION
  // ============================================================

  @FXML
  public void initialize() {
    loadCatalogs();
    setupActions();
    setupNumericFields();
    setupPriceField();
    setupDescriptionLimit();
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

  // ============================================================
  // INPUT RESTRICTIONS (TextFormatter)
  // ============================================================

  /**
   * Restringe un TextField a solo dígitos, con un máximo de {@code maxDigits}.
   * Bloquea tanto el tipeo como el pegado de contenido inválido.
   */
  private void setupNumericField(TextField field, int maxDigits) {
    field.setTextFormatter(new TextFormatter<>(change -> {
      String newText = change.getControlNewText();
      if (newText.isEmpty() || newText.matches("\\d{0," + maxDigits + "}")) {
        return change;
      }
      return null; // rechaza el cambio
    }));
  }

  /**
   * Restringe el campo de precio: solo dígitos y hasta un punto decimal
   * con un máximo de decimales configurado.
   */
  private void setupPriceField() {
    txtPrice.setTextFormatter(new TextFormatter<>(change -> {
      String newText = change.getControlNewText();

      // Vacío permitido (para poder limpiar)
      if (newText.isEmpty())
        return change;

      // Un solo punto decimal, hasta N decimales, hasta M enteros
      String regex = "\\d{0," + MAX_PRICE_INT_DIGITS + "}(\\.\\d{0," + MAX_PRICE_DECIMALS + "})?";
      if (newText.matches(regex)) {
        return change;
      }
      return null;
    }));
  }

  private void setupNumericFields() {
    setupNumericField(txtNumber, MAX_DIGITS_NUMBER);
    setupNumericField(txtFloor, MAX_DIGITS_FLOOR);
    setupNumericField(txtCapacity, MAX_DIGITS_CAPACITY);
  }

  private void setupDescriptionLimit() {
    // Bloquea la escritura una vez alcanzado el límite (aplica también a pegar).
    txtDescription.setTextFormatter(new TextFormatter<>(change -> {
      if (change.getControlNewText().length() <= MAX_DESCRIPTION_LENGTH) {
        return change;
      }
      return null;
    }));

    // Actualiza el contador en tiempo real.
    txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
      int len = newVal == null ? 0 : newVal.length();
      lblCharCounter.setText(len + "/" + MAX_DESCRIPTION_LENGTH + " caracteres");
      updateCounterStyle(len);
    });

    // Estado inicial por si el campo ya viniera con texto.
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

  // ============================================================
  // EDIT MODE
  // ============================================================

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

  // ============================================================
  // SAVE
  // ============================================================

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

  // ============================================================
  // VALIDATION
  // ============================================================

  /**
   * Valida un campo entero: no vacío, solo dígitos, y con longitud máxima.
   * Usa {@code .length()} ANTES de parsear para evitar NumberFormatException
   * cuando el valor excede Integer.MAX_VALUE.
   *
   * @return true si el campo es válido (seguro para parseInt)
   */
  private boolean validateIntegerField(String rawText,
      String fieldName,
      int maxDigits,
      StringBuilder errors) {
    String value = rawText == null ? "" : rawText.trim();

    // 1. No vacío
    if (value.isEmpty()) {
      logger.warn("Intento de dejar vacío el campo {}", fieldName);
      errors.append(fieldName).append(" es un valor obligatorio.\n");
      return false;
    }

    // 2. Solo dígitos (sin signo, sin decimales, sin espacios)
    if (!value.matches("\\d+")) {
      logger.error("Valor '{}' para {} no es un entero válido", value, fieldName);
      errors.append(fieldName).append(" debe ser un valor numérico entero.\n");
      return false;
    }

    // 3. Longitud máxima ANTES de parsear -> evita overflow
    if (value.length() > maxDigits) {
      logger.warn("Valor '{}' para {} supera los {} dígitos", value, fieldName, maxDigits);
      errors.append(fieldName)
          .append(" no puede tener más de ")
          .append(maxDigits)
          .append(" dígitos.\n");
      return false;
    }

    // Ahora es 100% seguro hacer Integer.parseInt
    return true;
  }

  private boolean validateFields() {
    StringBuilder errors = new StringBuilder();

    // ---- Número de habitación: máximo 4 dígitos (0..9999) ----
    if (validateIntegerField(txtNumber.getText(), "El número de habitación", MAX_DIGITS_NUMBER, errors)) {
      int n = Integer.parseInt(txtNumber.getText().trim());
      if (n < 1) {
        logger.warn("Intento de insertar valor '{}' menor que 1", txtNumber.getText());
        errors.append("El número debe ser un valor positivo.\n");
      }
    }

    // ---- Piso: máximo 3 dígitos (0..999) ----
    if (validateIntegerField(txtFloor.getText(), "El piso", MAX_DIGITS_FLOOR, errors)) {
      int f = Integer.parseInt(txtFloor.getText().trim());
      if (f < 0) {
        logger.warn("Intento de insertar valor '{}' negativo", txtFloor.getText());
        errors.append("El piso no puede ser un valor negativo.\n");
      }
    }

    // ---- Tipo de habitación ----
    if (comboType.getValue() == null) {
      logger.warn("Intento de no insertar ningun tipo de habitación");
      errors.append("Seleccione un tipo de habitación.\n");
    }

    // ---- Capacidad: máximo 3 dígitos (0..999) ----
    if (validateIntegerField(txtCapacity.getText(), "La capacidad", MAX_DIGITS_CAPACITY, errors)) {
      int c = Integer.parseInt(txtCapacity.getText().trim());
      if (c < 1) {
        logger.warn("Intento de insertar valor '{}' menor que 1", txtCapacity.getText());
        errors.append("La capacidad debe ser un valor positivo.\n");
      }
    }

    // ---- Vista ----
    if (comboView.getValue() == null) {
      logger.warn("Intento de no insertar vista");
      errors.append("Seleccione una vista.\n");
    }

    // ---- Precio ----
    if (txtPrice.getText().trim().isEmpty()) {
      logger.warn("Intento de no insertar precio");
      errors.append("El precio es obligatorio.\n");
    } else {
      try {
        double p = Double.parseDouble(txtPrice.getText().trim());
        if (p < 0) {
          logger.warn("Intento de insertar valor '{}' negativo", txtPrice.getText());
          errors.append("El precio no puede ser un valor negativo.\n");
        }
      } catch (NumberFormatException e) {
        errors.append("El precio debe ser un valor numérico (puede ser decimal).\n");
        logger.error("Intento de insertar valor '{}' que no es un número", txtPrice.getText());
      }
    }

    // ---- Descripción ----
    if (txtDescription.getText().length() > MAX_DESCRIPTION_LENGTH) {
      logger.warn("Intento de ingresar más de {} caracteres en la descripción.", MAX_DESCRIPTION_LENGTH);
      errors.append("La descripción no debe tener más de ")
          .append(MAX_DESCRIPTION_LENGTH)
          .append(" caracteres.\n");
    }

    if (errors.length() > 0) {
      showAlert("Validación", "Corrija los siguientes errores", errors.toString());
      return false;
    }
    return true;
  }

  // ============================================================
  // UI HELPERS
  // ============================================================

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
