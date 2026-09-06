package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.*;
import repositories.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
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
    @FXML private ComboBox<String> comboDepartment;
    @FXML private ComboBox<String> comboStatus;
    @FXML private DatePicker dateHireDate;
    @FXML private ComboBox<String> comboShiftName;   // antes era TextField: ahora lista fija Mañana/Tarde/Noche
    @FXML private TextField txtShiftStart;           // formato "HH:mm", ej: "07:00"
    @FXML private TextField txtShiftEnd;             // formato "HH:mm", ej: "15:00"
    @FXML private TextField txtSalary;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ========== VALIDACIÓN: patrones y constantes ==========
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} ]+$");            // solo letras (con tildes/ñ) y espacios
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{1,2}\\.\\d{3}\\.\\d{3}$"); // ej: 34.567.890
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\s-]+$");             // números, espacios y guiones
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final int MIN_AGE_YEARS = 18;
    private static final int MAX_STREET_LENGTH = 150;
    private static final int MAX_ADDRESS_NUMBER_LENGTH = 20;
    private static final int MAX_CITY_LENGTH = 100;

    // ========== DAOs AND MAPS ==========
    private StaffDAO staffDAO = new StaffDAO();
    private JobPositionDAO jobPositionDAO = new JobPositionDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();

    private Map<Integer, String> positions;
    private Map<Integer, String> departments;

    private Staff editingStaff; // null if creating new

    // ========== INITIALIZATION ==========
    @FXML
    public void initialize() {
        loadCatalogs();
        setupButtonActions();
    }

    // ========== LOAD METHODS ==========
    private void loadCatalogs() {
        try {
            positions = jobPositionDAO.listAll();
            comboPosition.getItems().setAll(positions.values());

            departments = departmentDAO.listAll();
            comboDepartment.getItems().setAll(departments.values());

            comboStatus.getItems().setAll("Activo", "Inactivo");

            comboShiftName.getItems().setAll("Mañana", "Tarde", "Noche");

        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los catálogos", e.getMessage());
        }
    }

    private void setupButtonActions() {
        btnCancel.setOnAction(e -> closeWindow());
        btnSave.setOnAction(e -> saveStaff());
    }

    // ========== PUBLIC METHOD TO SET STAFF FOR EDITING ==========
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
        comboDepartment.getSelectionModel().select(staff.getDepartmentName());
        comboStatus.getSelectionModel().select(staff.getStatus() == StaffStatus.ACTIVE ? "Activo" : "Inactivo");
        dateHireDate.setValue(staff.getHireDate());
        comboShiftName.getSelectionModel().select(staff.getShiftName());
        txtShiftStart.setText(staff.getShiftStart() != null ? staff.getShiftStart().toString() : "");
        txtShiftEnd.setText(staff.getShiftEnd() != null ? staff.getShiftEnd().toString() : "");
        txtSalary.setText(staff.getSalary() != null ? staff.getSalary().toString() : "");
    }

    // ========== SAVE METHODS ==========
    private void saveStaff() {
        if (!validateFields()) return;

        Staff staff = editingStaff != null ? editingStaff : new Staff();
        loadDataFromForm(staff);

        try {
            // Validamos DNI duplicado solo al crear uno nuevo
            if (editingStaff == null) {
                Staff existente = staffDAO.findByDni(staff.getDni());
                if (existente != null) {
                    showAlert("Validación", "DNI duplicado",
                            "Ya existe un empleado registrado con ese DNI.");
                    return;
                }
            }

            boolean success;
            if (editingStaff != null) {
                success = staffDAO.isUpdate(staff);
            } else {
                success = staffDAO.isInsert(staff);
            }

            if (success) {
                showAlert("Éxito", "Personal guardado",
                        editingStaff != null ? "El empleado ha sido actualizado correctamente."
                                : "El empleado se ha creado correctamente.");
                closeWindow();
            }
        } catch (SQLException e) {
            showAlert("Error", "No se pudo guardar el empleado", e.getMessage());
        }
    }

    // ========== HELPER METHODS ==========
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
        staff.setIdDepartment(getIdBySelection(comboDepartment, departments));
        staff.setStatus("Activo".equals(comboStatus.getSelectionModel().getSelectedItem())
                ? StaffStatus.ACTIVE : StaffStatus.INACTIVE);

        staff.setHireDate(dateHireDate.getValue());
        staff.setShiftName(comboShiftName.getSelectionModel().getSelectedItem());
        staff.setShiftStart(parseHora(txtShiftStart.getText()));
        staff.setShiftEnd(parseHora(txtShiftEnd.getText()));
        staff.setSalary(new java.math.BigDecimal(txtSalary.getText().trim()));
    }

    // Convierte texto "HH:mm" a LocalTime; devuelve null si está vacío
    private LocalTime parseHora(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        return LocalTime.parse(texto.trim());
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

    // ========== VALIDACIÓN COMPLETA ==========
    private boolean validateFields() {

        // --- Nombre y Apellido: obligatorios, solo letras y espacios ---
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Validación", "Nombre y Apellido son obligatorios", "Complete los campos marcados con *");
            return false;
        }
        if (!NAME_PATTERN.matcher(firstName).matches() || !NAME_PATTERN.matcher(lastName).matches()) {
            showAlert("Validación", "Nombre/Apellido inválido", "Solo se permiten letras y espacios (sin números ni símbolos).");
            return false;
        }

        // --- DNI: obligatorio, formato 12.345.678 ---
        String dni = txtDni.getText().trim();
        if (dni.isEmpty()) {
            showAlert("Validación", "El DNI es obligatorio", "");
            return false;
        }
        if (!DNI_PATTERN.matcher(dni).matches()) {
            showAlert("Validación", "Formato de DNI inválido", "Usá el formato 12.345.678 (con puntos).");
            return false;
        }

        // --- Teléfono: opcional, pero si se completa debe tener formato válido ---
        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showAlert("Validación", "Formato de teléfono inválido", "Solo números, espacios y guiones (ej: 3541-123456).");
            return false;
        }

        // --- Email: opcional, pero si se completa debe tener formato válido ---
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showAlert("Validación", "Formato de email inválido", "Ejemplo válido: nombre@dominio.com");
            return false;
        }

        // --- Fecha de nacimiento: obligatoria, edad mínima 18 años ---
        LocalDate birthDate = dateBirthDate.getValue();
        if (birthDate == null) {
            showAlert("Validación", "La Fecha de Nacimiento es obligatoria", "");
            return false;
        }
        if (birthDate.plusYears(MIN_AGE_YEARS).isAfter(LocalDate.now())) {
            showAlert("Validación", "Edad mínima no cumplida", "El empleado debe tener al menos " + MIN_AGE_YEARS + " años.");
            return false;
        }

        // --- Dirección: topes de largo según la base de datos ---
        if (txtStreet.getText().trim().length() > MAX_STREET_LENGTH) {
            showAlert("Validación", "Calle demasiado larga", "Máximo " + MAX_STREET_LENGTH + " caracteres.");
            return false;
        }
        if (txtAddressNumber.getText().trim().length() > MAX_ADDRESS_NUMBER_LENGTH) {
            showAlert("Validación", "Número demasiado largo", "Máximo " + MAX_ADDRESS_NUMBER_LENGTH + " caracteres.");
            return false;
        }
        if (txtCity.getText().trim().length() > MAX_CITY_LENGTH) {
            showAlert("Validación", "Ciudad demasiado larga", "Máximo " + MAX_CITY_LENGTH + " caracteres.");
            return false;
        }

        // --- Cargo / Área / Estado: obligatorios (selección de combo) ---
        if (comboPosition.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccione un Cargo", "");
            return false;
        }
        if (comboDepartment.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccione un Área", "");
            return false;
        }
        if (comboStatus.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccione un Estado", "");
            return false;
        }

        // --- Fecha de ingreso: obligatoria, no futura, no antes de los 18 años del empleado ---
        LocalDate hireDate = dateHireDate.getValue();
        if (hireDate == null) {
            showAlert("Validación", "La Fecha de Ingreso es obligatoria", "");
            return false;
        }
        if (hireDate.isAfter(LocalDate.now())) {
            showAlert("Validación", "Fecha de Ingreso inválida", "No puede ser una fecha futura.");
            return false;
        }
        if (hireDate.isBefore(birthDate.plusYears(MIN_AGE_YEARS))) {
            showAlert("Validación", "Fecha de Ingreso inválida",
                    "No puede ser anterior a la fecha en que el empleado cumplió " + MIN_AGE_YEARS + " años.");
            return false;
        }

        // --- Turno: nombre obligatorio (lista fija), horarios con formato válido ---
        if (comboShiftName.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccione un Turno", "");
            return false;
        }
        try {
            if (!txtShiftStart.getText().trim().isEmpty()) {
                LocalTime.parse(txtShiftStart.getText().trim());
            }
            if (!txtShiftEnd.getText().trim().isEmpty()) {
                LocalTime.parse(txtShiftEnd.getText().trim());
            }
        } catch (DateTimeParseException e) {
            showAlert("Validación", "Horario de turno inválido", "Usá el formato HH:mm, ej: 07:00");
            return false;
        }
        // Nota: no se exige que la hora de fin sea posterior a la de inicio,
        // para permitir turnos que cruzan la medianoche (ej: Noche 22:00 - 06:00).

        // --- Salario: obligatorio, mayor a 0 ---
        try {
            java.math.BigDecimal salario = new java.math.BigDecimal(txtSalary.getText().trim());
            if (salario.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                showAlert("Validación", "Salario inválido", "El salario debe ser mayor a $0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validación", "El Salario no es válido", "Ingrese solo números, ej: 450000");
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