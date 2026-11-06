package controllers;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.*;
import repositories.*;
import utils.Alerts;
import utils.StyleManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class StaffFormController {

  // ========== FORM COMPONENTS ==========
  @FXML private Label lblFormTitle;
  @FXML private TextField txtFirstName;
  @FXML private TextField txtLastName;
  @FXML private TextField txtDni;
  @FXML private DatePicker dateBirthDate;
  @FXML private TextField txtPhone;
  @FXML private TextField txtEmail;
  @FXML private TextField txtStreet;
  @FXML private TextField txtAddressNumber;
  @FXML private TextField txtCity;
  @FXML private ComboBox<String> comboPosition;
  @FXML private DatePicker dateHireDate;
  @FXML private ComboBox<String> comboShiftName;
  @FXML private TextField txtShiftStart;
  @FXML private TextField txtShiftEnd;
  @FXML private TextField txtSalary;
  @FXML private Label lblSalaryCounter;

  @FXML private Button btnSave;
  @FXML private Button btnCancel;

  // ========== VALIDACIÓN: patrones y constantes ==========
  private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} ]+$");
  private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{7,9}$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\s-]+$");
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
  private static final int MIN_AGE_YEARS = 18;
  private static final int MAX_STREET_LENGTH = 150;
  private static final int MAX_ADDRESS_NUMBER_LENGTH = 20;
  private static final int MAX_CITY_LENGTH = 100;
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final java.math.BigDecimal SALARY_MAX = new java.math.BigDecimal("9999999999.99");
  private static final Pattern SALARY_TYPING_PATTERN = Pattern.compile("^\\d{0,10}(\\.\\d{0,2})?$");
  private static final Pattern DNI_TYPING_PATTERN = Pattern.compile("^\\d{0,9}$");
  private static final Pattern PHONE_TYPING_PATTERN = Pattern.compile("^[0-9\\s-]{0,20}$");

  // ========== DAOs Y CATÁLOGOS ==========
  private final StaffDAO staffDAO = new StaffDAO();
  private final JobPositionDAO jobPositionDAO = new JobPositionDAO();
  private final ShiftDAO shiftDAO = new ShiftDAO();

  private Map<Integer, String> positions;
  private Map<Integer, String> shifts;

  private Staff editingStaff;

  /** Campos ya tocados por el usuario (para mostrar feedback visual). */
  private final Set<Control> touchedFields = new HashSet<>();

  // ========== INIT ==========
  @FXML
  public void initialize() {
    loadCatalogs();
    setupDateFormat();
    setupDniFormatter();
    setupPhoneFormatter();
    setupSalaryFormatter();
    setupButtonActions();
    setupValidations();
    btnSave.setDisable(true);
  }

  // ========== FORMATTERS ==========
  private void setupDniFormatter() {
    UnaryOperator<TextFormatter.Change> filtro = change -> {
      String nuevo = change.getControlNewText();
      if (nuevo.isEmpty() || DNI_TYPING_PATTERN.matcher(nuevo).matches()) return change;
      return null;
    };
    txtDni.setTextFormatter(new TextFormatter<>(filtro));
  }

  private void setupPhoneFormatter() {
    UnaryOperator<TextFormatter.Change> filtro = change -> {
      String nuevo = change.getControlNewText();
      if (nuevo.isEmpty() || PHONE_TYPING_PATTERN.matcher(nuevo).matches()) return change;
      return null;
    };
    txtPhone.setTextFormatter(new TextFormatter<>(filtro));
  }

  private void setupSalaryFormatter() {
    UnaryOperator<TextFormatter.Change> filtro = change -> {
      String nuevo = change.getControlNewText();
      if (nuevo.isEmpty() || SALARY_TYPING_PATTERN.matcher(nuevo).matches()) return change;
      return null;
    };
    txtSalary.setTextFormatter(new TextFormatter<>(filtro));

    txtSalary.textProperty().addListener((obs, oldV, newV) -> {
      long digitos = newV.chars().filter(Character::isDigit).count();
      lblSalaryCounter.setText(digitos + "/10");
    });
  }

  private void loadCatalogs() {
    try {
      positions = jobPositionDAO.listAll();
      comboPosition.getItems().setAll(positions.values());

      shifts = shiftDAO.listAll();
      comboShiftName.getItems().add("");
      comboShiftName.getItems().addAll(shifts.values());
    } catch (SQLException e) {
      showAlert("No se pudieron cargar los catálogos: " + e.getMessage());
    }
  }

  private void setupDateFormat() {
    StringConverter<LocalDate> converter = new StringConverter<>() {
      @Override public String toString(LocalDate date) {
        return date != null ? DATE_FORMAT.format(date) : "";
      }
      @Override public LocalDate fromString(String text) {
        return (text == null || text.trim().isEmpty()) ? null : LocalDate.parse(text, DATE_FORMAT);
      }
    };
    dateBirthDate.setConverter(converter);
    dateBirthDate.setPromptText("dd/mm/aaaa");
    dateHireDate.setConverter(converter);
    dateHireDate.setPromptText("dd/mm/aaaa");
  }

  private void setupButtonActions() {
    btnCancel.setOnAction(e -> {
      boolean ok = StyleManager.showConfirmation(
          "Cancelar",
          "¿Descartar cambios?",
          "Perderá todo lo que haya modificado o agregado en el formulario.");
      if (ok) closeWindow();
    });
    btnSave.setOnAction(e -> saveStaff());
  }

  // ========== VALIDACIONES EN VIVO (patrón RoomForm / CustomerForm) ==========
  private void setupValidations() {
    // --- TextFields: validar al perder foco ---
    txtFirstName.focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtFirstName); validateFirstName(); } });
    txtLastName .focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtLastName);  validateLastName();  } });
    txtDni      .focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtDni);       validateDni();       } });
    txtPhone    .focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtPhone);     validatePhone();     } });
    txtEmail    .focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtEmail);     validateEmail();     } });
    txtStreet   .focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtStreet);    validateStreet();    } });
    txtAddressNumber.focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtAddressNumber); validateAddressNumber(); } });
    txtCity     .focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtCity);      validateCity();      } });
    txtShiftStart.focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtShiftStart); validateShiftStart(); } });
    txtShiftEnd .focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtShiftEnd);  validateShiftEnd();  } });
    txtSalary   .focusedProperty().addListener((o, a, b) -> { if (!b) { touchedFields.add(txtSalary);    validateSalary();    } });

    // --- DatePickers: validar al cambiar valor + marcar como tocado ---
    dateBirthDate.valueProperty().addListener((o, a, b) -> {
      if (b != null) touchedFields.add(dateBirthDate);
      validateBirthDate();
      updateSaveButtonState();
    });
    dateHireDate.valueProperty().addListener((o, a, b) -> {
      if (b != null) touchedFields.add(dateHireDate);
      validateHireDate();
      updateSaveButtonState();
    });

    // --- ComboBox: marcar como tocado al seleccionar ---
    comboPosition.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
      if (b != null) touchedFields.add(comboPosition);
      updateSaveButtonState();
    });
    comboShiftName.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
      if (b != null) touchedFields.add(comboShiftName);
      updateSaveButtonState();
    });

    // --- Mientras escribe: solo actualizar botón ---
    txtFirstName.textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtLastName .textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtDni      .textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtPhone    .textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtEmail    .textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtStreet   .textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtAddressNumber.textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtCity     .textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtShiftStart.textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtShiftEnd .textProperty().addListener((o, a, b) -> updateSaveButtonState());
    txtSalary   .textProperty().addListener((o, a, b) -> updateSaveButtonState());
  }

  // ========== VALIDADORES INDIVIDUALES (con feedback) ==========
  private boolean validateFirstName() {
    String v = txtFirstName.getText() == null ? "" : txtFirstName.getText().trim();
    boolean ok = false;
    String err = null;
    if (v.isEmpty()) {
      err = "El Nombre es obligatorio.";
    } else if (!NAME_PATTERN.matcher(v).matches()) {
      err = "El Nombre solo puede contener letras y espacios.";
    } else ok = true;
    setFieldValid(txtFirstName, ok, err);
    return ok;
  }

  private boolean validateLastName() {
    String v = txtLastName.getText() == null ? "" : txtLastName.getText().trim();
    boolean ok = false;
    String err = null;
    if (v.isEmpty()) {
      err = "El Apellido es obligatorio.";
    } else if (!NAME_PATTERN.matcher(v).matches()) {
      err = "El Apellido solo puede contener letras y espacios.";
    } else ok = true;
    setFieldValid(txtLastName, ok, err);
    return ok;
  }

  private boolean validateDni() {
    String v = txtDni.getText() == null ? "" : txtDni.getText().trim();
    boolean ok = false;
    String err = null;
    if (v.isEmpty()) {
      err = "El DNI es obligatorio.";
    } else if (!DNI_PATTERN.matcher(v).matches()) {
      err = "El DNI debe contener solo números (7 a 9 dígitos).";
    } else ok = true;
    setFieldValid(txtDni, ok, err);
    return ok;
  }

  private boolean validateBirthDate() {
    LocalDate bd = dateBirthDate.getValue();
    boolean ok = false;
    String err = null;
    if (bd == null) {
      err = "La Fecha de Nacimiento es obligatoria.";
    } else if (bd.plusYears(MIN_AGE_YEARS).isAfter(LocalDate.now())) {
      err = "El empleado debe tener al menos " + MIN_AGE_YEARS + " años.";
    } else ok = true;
    setFieldValid(dateBirthDate, ok, err);
    return ok;
  }

  private boolean validatePhone() {
    String v = txtPhone.getText() == null ? "" : txtPhone.getText().trim();
    boolean ok = false;
    String err = null;
    if (v.isEmpty()) {
      err = "El Teléfono es obligatorio.";
    } else if (!PHONE_PATTERN.matcher(v).matches()) {
      err = "El Teléfono solo puede contener números, espacios y guiones.";
    } else ok = true;
    setFieldValid(txtPhone, ok, err);
    return ok;
  }

  private boolean validateEmail() {
    String v = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
    boolean ok = true;
    String err = null;
    if (!v.isEmpty() && !EMAIL_PATTERN.matcher(v).matches()) {
      err = "El formato de Email no es válido (ej: nombre@dominio.com).";
      ok = false;
    }
    setFieldValid(txtEmail, ok, err);
    return ok;
  }

  private boolean validateStreet() {
    String v = txtStreet.getText() == null ? "" : txtStreet.getText().trim();
    boolean ok = false;
    String err = null;
    if (v.isEmpty()) {
      err = "La Calle es obligatoria.";
    } else if (v.length() > MAX_STREET_LENGTH) {
      err = "La Calle no puede superar los " + MAX_STREET_LENGTH + " caracteres.";
    } else ok = true;
    setFieldValid(txtStreet, ok, err);
    return ok;
  }

  private boolean validateAddressNumber() {
    String v = txtAddressNumber.getText() == null ? "" : txtAddressNumber.getText().trim();
    boolean ok = v.length() <= MAX_ADDRESS_NUMBER_LENGTH;
    String err = ok ? null : "El Número no puede superar los " + MAX_ADDRESS_NUMBER_LENGTH + " caracteres.";
    setFieldValid(txtAddressNumber, ok, err);
    return ok;
  }

  private boolean validateCity() {
    String v = txtCity.getText() == null ? "" : txtCity.getText().trim();
    boolean ok = false;
    String err = null;
    if (v.isEmpty()) {
      err = "La Ciudad es obligatoria.";
    } else if (v.length() > MAX_CITY_LENGTH) {
      err = "La Ciudad no puede superar los " + MAX_CITY_LENGTH + " caracteres.";
    } else ok = true;
    setFieldValid(txtCity, ok, err);
    return ok;
  }

  private boolean validateHireDate() {
    LocalDate hd = dateHireDate.getValue();
    LocalDate bd = dateBirthDate.getValue();
    boolean ok = false;
    String err = null;
    if (hd == null) {
      err = "La Fecha de Ingreso es obligatoria.";
    } else if (hd.isAfter(LocalDate.now())) {
      err = "La Fecha de Ingreso no puede ser una fecha futura.";
    } else if (bd != null && hd.isBefore(bd.plusYears(MIN_AGE_YEARS))) {
      err = "La Fecha de Ingreso no puede ser anterior a que el empleado cumpliera " + MIN_AGE_YEARS + " años.";
    } else ok = true;
    setFieldValid(dateHireDate, ok, err);
    return ok;
  }

  private boolean validateShiftStart() {
    String v = txtShiftStart.getText() == null ? "" : txtShiftStart.getText().trim();
    boolean ok = true;
    String err = null;
    if (!v.isEmpty()) {
      try { LocalTime.parse(v); }
      catch (DateTimeParseException e) {
        ok = false;
        err = "La Hora de inicio debe tener formato HH:mm (ej: 07:00).";
      }
    }
    setFieldValid(txtShiftStart, ok, err);
    return ok;
  }

  private boolean validateShiftEnd() {
    String v = txtShiftEnd.getText() == null ? "" : txtShiftEnd.getText().trim();
    boolean ok = true;
    String err = null;
    if (!v.isEmpty()) {
      try { LocalTime.parse(v); }
      catch (DateTimeParseException e) {
        ok = false;
        err = "La Hora de fin debe tener formato HH:mm (ej: 15:00).";
      }
    }
    setFieldValid(txtShiftEnd, ok, err);
    return ok;
  }

  private boolean validateSalary() {
    String v = txtSalary.getText() == null ? "" : txtSalary.getText().trim();
    boolean ok = false;
    String err = null;
    if (v.isEmpty()) {
      err = "El Salario es obligatorio.";
    } else {
      try {
        java.math.BigDecimal s = new java.math.BigDecimal(v);
        if (s.compareTo(java.math.BigDecimal.ZERO) <= 0) {
          err = "El Salario debe ser mayor a $0.";
        } else if (s.compareTo(SALARY_MAX) > 0) {
          err = "El Salario no puede superar $" + SALARY_MAX + ".";
        } else ok = true;
      } catch (NumberFormatException e) {
        err = "El Salario debe ser un número válido (ej: 450000).";
      }
    }
    setFieldValid(txtSalary, ok, err);
    return ok;
  }

  // ========== FEEDBACK VISUAL (mismo estilo que los otros forms) ==========
  private void setFieldValid(Control field, boolean valid, String errorMessage) {
    if (!touchedFields.contains(field)) return;

    if (valid) {
      field.setStyle("");
      field.setTooltip(null);
    } else {
      field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
      if (errorMessage != null) field.setTooltip(new Tooltip(errorMessage));
    }
  }

  // ========== HELPERS SILENCIOSOS (para habilitar/deshabilitar botón) ==========
  private boolean isValidName(String v) {
    if (v == null) return false;
    v = v.trim();
    return !v.isEmpty() && NAME_PATTERN.matcher(v).matches();
  }
  private boolean isValidDni(String v) {
    if (v == null) return false;
    return DNI_PATTERN.matcher(v.trim()).matches();
  }
  private boolean isValidPhone(String v) {
    if (v == null) return false;
    v = v.trim();
    return !v.isEmpty() && PHONE_PATTERN.matcher(v).matches();
  }
  private boolean isValidEmail(String v) {
    if (v == null) return true;
    v = v.trim();
    return v.isEmpty() || EMAIL_PATTERN.matcher(v).matches();
  }
  private boolean isValidSalary(String v) {
    if (v == null) return false;
    v = v.trim();
    if (v.isEmpty()) return false;
    try {
      java.math.BigDecimal s = new java.math.BigDecimal(v);
      return s.compareTo(java.math.BigDecimal.ZERO) > 0 && s.compareTo(SALARY_MAX) <= 0;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private void updateSaveButtonState() {
    boolean ok =
        isValidName(txtFirstName.getText()) &&
        isValidName(txtLastName.getText()) &&
        isValidDni(txtDni.getText()) &&
        dateBirthDate.getValue() != null &&
        !dateBirthDate.getValue().plusYears(MIN_AGE_YEARS).isAfter(LocalDate.now()) &&
        isValidPhone(txtPhone.getText()) &&
        isValidEmail(txtEmail.getText()) &&
        txtStreet.getText() != null && !txtStreet.getText().trim().isEmpty() &&
        txtStreet.getText().trim().length() <= MAX_STREET_LENGTH &&
        (txtAddressNumber.getText() == null ||
            txtAddressNumber.getText().trim().length() <= MAX_ADDRESS_NUMBER_LENGTH) &&
        txtCity.getText() != null && !txtCity.getText().trim().isEmpty() &&
        txtCity.getText().trim().length() <= MAX_CITY_LENGTH &&
        !comboPosition.getSelectionModel().isEmpty() &&
        dateHireDate.getValue() != null &&
        !dateHireDate.getValue().isAfter(LocalDate.now()) &&
        isValidSalary(txtSalary.getText());

    btnSave.setDisable(!ok);
  }

  // ========== EDICIÓN ==========
  public void setStaff(Staff staff) {
    this.editingStaff = staff;
    lblFormTitle.setText("Editar Personal");
    txtFirstName.setText(staff.getFirstName());
    txtLastName.setText(staff.getLastName());
    txtDni.setText(staff.getDni());
    dateBirthDate.setValue(staff.getBirthDate());
    txtPhone.setText(staff.getPhone());
    txtEmail.setText(staff.getEmail());
    txtStreet.setText(staff.getStreet());
    txtAddressNumber.setText(staff.getAddressNumber());
    txtCity.setText(staff.getCity());
    comboPosition.getSelectionModel().select(staff.getPositionName());
    dateHireDate.setValue(staff.getHireDate());
    comboShiftName.getSelectionModel().select(staff.getShiftName() != null ? staff.getShiftName() : "");
    txtShiftStart.setText(staff.getShiftStart() != null ? staff.getShiftStart().toString() : "");
    txtShiftEnd.setText(staff.getShiftEnd() != null ? staff.getShiftEnd().toString() : "");
    txtSalary.setText(staff.getSalary() != null ? staff.getSalary().toString() : "");

    updateSaveButtonState();
  }

  // ========== GUARDAR ==========
  private void saveStaff() {
    try {
      if (!validateAllFields()) return;

      Staff staff = editingStaff != null ? editingStaff : new Staff();
      loadDataFromForm(staff);

      if (editingStaff == null) {
        Staff existente = staffDAO.findByDni(staff.getDni());
        if (existente != null) {
          showAlert("Ya existe un empleado registrado con ese DNI.");
          return;
        }
      }

      boolean success = editingStaff != null ? staffDAO.isUpdate(staff) : staffDAO.isInsert(staff);
      if (success) {
        showAlert(editingStaff != null
            ? "El empleado ha sido actualizado correctamente."
            : "El empleado se ha creado correctamente.");
        closeWindow();
      }
    } catch (SQLException e) {
      showAlert("No se pudo guardar el empleado: " + e.getMessage());
    } catch (Exception e) {
      showAlert("Ocurrió un error inesperado: " + e.getMessage());
    }
  }

  private boolean validateAllFields() {
    // Marcar todos como tocados para que el feedback se aplique
    touchedFields.add(txtFirstName);
    touchedFields.add(txtLastName);
    touchedFields.add(txtDni);
    touchedFields.add(dateBirthDate);
    touchedFields.add(txtPhone);
    touchedFields.add(txtEmail);
    touchedFields.add(txtStreet);
    touchedFields.add(txtAddressNumber);
    touchedFields.add(txtCity);
    touchedFields.add(comboPosition);
    touchedFields.add(dateHireDate);
    touchedFields.add(txtShiftStart);
    touchedFields.add(txtShiftEnd);
    touchedFields.add(txtSalary);

    boolean ok = true;
    ok &= validateFirstName();
    ok &= validateLastName();
    ok &= validateDni();
    ok &= validateBirthDate();
    ok &= validatePhone();
    ok &= validateEmail();
    ok &= validateStreet();
    ok &= validateAddressNumber();
    ok &= validateCity();

    // Combos / DatePicker
    if (comboPosition.getSelectionModel().isEmpty()) {
      setFieldValid(comboPosition, false, "Debe seleccionar un Cargo.");
      ok = false;
    } else {
      setFieldValid(comboPosition, true, null);
    }
    ok &= validateHireDate();

    ok &= validateShiftStart();
    ok &= validateShiftEnd();
    ok &= validateSalary();

    return ok;
  }

  // ========== CARGA DE DATOS AL MODELO ==========
  private void loadDataFromForm(Staff staff) {
    staff.setFirstName(txtFirstName.getText().trim());
    staff.setLastName(txtLastName.getText().trim());
    staff.setDni(txtDni.getText().trim());
    staff.setBirthDate(dateBirthDate.getValue());
    staff.setPhone(txtPhone.getText().trim());
    staff.setEmail(txtEmail.getText().trim());
    staff.setStreet(txtStreet.getText().trim());
    staff.setAddressNumber(txtAddressNumber.getText().trim());
    staff.setCity(txtCity.getText().trim());
    staff.setIdPosition(getIdBySelection(comboPosition, positions));

    if (staff.getStatus() == null) staff.setStatus(StaffStatus.ACTIVE);

    staff.setHireDate(dateHireDate.getValue());

    String shiftSelected = comboShiftName.getSelectionModel().getSelectedItem();
    staff.setIdShift((shiftSelected == null || shiftSelected.isEmpty())
        ? null
        : getIdBySelection(comboShiftName, shifts));

    staff.setShiftStart(parseHora(txtShiftStart.getText()));
    staff.setShiftEnd(parseHora(txtShiftEnd.getText()));
    staff.setSalary(new java.math.BigDecimal(txtSalary.getText().trim()));
  }

  private LocalTime parseHora(String texto) {
    if (texto == null || texto.trim().isEmpty()) return null;
    return LocalTime.parse(texto.trim());
  }

  private int getIdBySelection(ComboBox<String> combo, Map<Integer, String> map) {
    String selected = combo.getSelectionModel().getSelectedItem();
    if (selected == null) return 0;
    for (Map.Entry<Integer, String> entry : map.entrySet()) {
      if (entry.getValue().equals(selected)) return entry.getKey();
    }
    return 0;
  }

  // ========== VENTANA / ALERTAS ==========
  private void closeWindow() {
    Stage stage = (Stage) btnCancel.getScene().getWindow();
    stage.close();
  }

  private void showAlert(String mensaje) {
    Alerts.info("Validación", mensaje);
  }
}
