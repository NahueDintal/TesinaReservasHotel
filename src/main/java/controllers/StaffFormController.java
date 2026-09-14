package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.*;
import repositories.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
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
    @FXML private ComboBox<String> comboShiftName; // opcional: catálogo Mañana/Tarde/Noche
    @FXML private TextField txtShiftStart;
    @FXML private TextField txtShiftEnd;
    @FXML private TextField txtSalary;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ========== VALIDACIÓN: patrones y constantes ==========
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} ]+$");
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{7,9}$");        // solo números
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\s-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final int MIN_AGE_YEARS = 18;
    private static final int MAX_STREET_LENGTH = 150;
    private static final int MAX_ADDRESS_NUMBER_LENGTH = 20;
    private static final int MAX_CITY_LENGTH = 100;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ========== DAOs Y CATÁLOGOS ==========
    private StaffDAO staffDAO = new StaffDAO();
    private JobPositionDAO jobPositionDAO = new JobPositionDAO();
    private ShiftDAO shiftDAO = new ShiftDAO();

    private Map<Integer, String> positions;
    private Map<Integer, String> shifts;

    private Staff editingStaff; // null si es alta nueva

    @FXML
    public void initialize() {
        loadCatalogs();
        setupDateFormat();
        setupButtonActions();
    }

    private void loadCatalogs() {
        try {
            positions = jobPositionDAO.listAll();
            comboPosition.getItems().setAll(positions.values());

            shifts = shiftDAO.listAll();
            comboShiftName.getItems().add(""); // opción vacía: el turno es opcional
            comboShiftName.getItems().addAll(shifts.values());

        } catch (SQLException e) {
            showAlert("No se pudieron cargar los catálogos: " + e.getMessage());
        }
    }

    // Muestra las fechas en formato DD/MM/AAAA en vez del ISO por defecto
    private void setupDateFormat() {
        StringConverter<LocalDate> converter = new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_FORMAT.format(date) : "";
            }

            @Override
            public LocalDate fromString(String text) {
                return (text == null || text.trim().isEmpty()) ? null : LocalDate.parse(text, DATE_FORMAT);
            }
        };
        dateBirthDate.setConverter(converter);
        dateBirthDate.setPromptText("dd/mm/aaaa");
        dateHireDate.setConverter(converter);
        dateHireDate.setPromptText("dd/mm/aaaa");
    }

    private void setupButtonActions() {
        btnCancel.setOnAction(e -> closeWindow());
        btnSave.setOnAction(e -> saveStaff());
    }

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
    }

    private void saveStaff() {
        // Red de seguridad: cualquier excepción inesperada muestra un aviso
        // en vez de dejar el botón "sin responder" (bug reportado en los test cases).
        try {
            if (!validateFields()) return;

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
                showAlert(editingStaff != null ? "El empleado ha sido actualizado correctamente."
                        : "El empleado se ha creado correctamente.");
                closeWindow();
            }
        } catch (SQLException e) {
            showAlert("No se pudo guardar el empleado: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Ocurrió un error inesperado: " + e.getMessage());
        }
    }

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

        if (staff.getStatus() == null) {
            staff.setStatus(StaffStatus.ACTIVE); // toda alta nace Activa; en edición no se toca desde acá
        }

        staff.setHireDate(dateHireDate.getValue());

        String shiftSelected = comboShiftName.getSelectionModel().getSelectedItem();
        staff.setIdShift(getIdBySelection(comboShiftName, shifts) == 0 || shiftSelected == null || shiftSelected.isEmpty()
                ? null : getIdBySelection(comboShiftName, shifts));

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
            if (entry.getValue().equals(selected)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    // ========== VALIDACIÓN ==========
    // Cada campo obligatorio se valida por separado y muestra SOLO el mensaje
    // correspondiente a lo que realmente falta (sin mezclar campos que sí están completos).
    private boolean validateFields() {

        if (txtFirstName.getText().trim().isEmpty()) {
            showAlert("El Nombre es obligatorio.");
            return false;
        }
        if (!NAME_PATTERN.matcher(txtFirstName.getText().trim()).matches()) {
            showAlert("El Nombre solo puede contener letras y espacios.");
            return false;
        }

        if (txtLastName.getText().trim().isEmpty()) {
            showAlert("El Apellido es obligatorio.");
            return false;
        }
        if (!NAME_PATTERN.matcher(txtLastName.getText().trim()).matches()) {
            showAlert("El Apellido solo puede contener letras y espacios.");
            return false;
        }

        String dni = txtDni.getText().trim();
        if (dni.isEmpty()) {
            showAlert("El DNI es obligatorio.");
            return false;
        }
        if (!DNI_PATTERN.matcher(dni).matches()) {
            showAlert("El DNI debe contener solo números (7 a 9 dígitos).");
            return false;
        }

        LocalDate birthDate = dateBirthDate.getValue();
        if (birthDate == null) {
            showAlert("La Fecha de Nacimiento es obligatoria.");
            return false;
        }
        if (birthDate.plusYears(MIN_AGE_YEARS).isAfter(LocalDate.now())) {
            showAlert("El empleado debe tener al menos " + MIN_AGE_YEARS + " años.");
            return false;
        }

        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            showAlert("El Teléfono es obligatorio.");
            return false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            showAlert("El Teléfono solo puede contener números, espacios y guiones.");
            return false;
        }

        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showAlert("El formato de Email no es válido (ejemplo: nombre@dominio.com).");
            return false;
        }

        String street = txtStreet.getText().trim();
        if (street.isEmpty()) {
            showAlert("La Calle es obligatoria.");
            return false;
        }
        if (street.length() > MAX_STREET_LENGTH) {
            showAlert("La Calle no puede superar los " + MAX_STREET_LENGTH + " caracteres.");
            return false;
        }

        if (txtAddressNumber.getText().trim().length() > MAX_ADDRESS_NUMBER_LENGTH) {
            showAlert("El Número no puede superar los " + MAX_ADDRESS_NUMBER_LENGTH + " caracteres.");
            return false;
        }

        String city = txtCity.getText().trim();
        if (city.isEmpty()) {
            showAlert("La Ciudad es obligatoria.");
            return false;
        }
        if (city.length() > MAX_CITY_LENGTH) {
            showAlert("La Ciudad no puede superar los " + MAX_CITY_LENGTH + " caracteres.");
            return false;
        }

        if (comboPosition.getSelectionModel().isEmpty()) {
            showAlert("Debe seleccionar un Cargo.");
            return false;
        }

        LocalDate hireDate = dateHireDate.getValue();
        if (hireDate == null) {
            showAlert("La Fecha de Ingreso es obligatoria.");
            return false;
        }
        if (hireDate.isAfter(LocalDate.now())) {
            showAlert("La Fecha de Ingreso no puede ser una fecha futura.");
            return false;
        }
        if (hireDate.isBefore(birthDate.plusYears(MIN_AGE_YEARS))) {
            showAlert("La Fecha de Ingreso no puede ser anterior a que el empleado cumpliera " + MIN_AGE_YEARS + " años.");
            return false;
        }

        // Turno: opcional. Si se completa alguna hora, validamos el formato.
        try {
            if (!txtShiftStart.getText().trim().isEmpty()) LocalTime.parse(txtShiftStart.getText().trim());
            if (!txtShiftEnd.getText().trim().isEmpty()) LocalTime.parse(txtShiftEnd.getText().trim());
        } catch (DateTimeParseException e) {
            showAlert("El horario del turno debe tener formato HH:mm (ej: 07:00).");
            return false;
        }

        String salaryText = txtSalary.getText().trim();
        if (salaryText.isEmpty()) {
            showAlert("El Salario es obligatorio.");
            return false;
        }
        try {
            java.math.BigDecimal salario = new java.math.BigDecimal(salaryText);
            if (salario.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                showAlert("El Salario debe ser mayor a $0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("El Salario debe ser un número válido (ejemplo: 450000).");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alert.showAndWait();
    }
}