package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.*;
import repositories.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;

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

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // Estilo para marcar un campo con error (borde rojo)
    private static final String ESTILO_INVALIDO = "-fx-border-color: #e53935; -fx-border-width: 2px; -fx-border-radius: 3px;";
    private static final String ESTILO_VALIDO = "";

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
    private static final java.math.BigDecimal SALARY_MAX = new java.math.BigDecimal("9999999999.99"); // límite real de la columna DECIMAL(12,2)
    private static final Pattern SALARY_TYPING_PATTERN = Pattern.compile("^\\d{0,10}(\\.\\d{0,2})?$");

    // ========== DAOs Y CATÁLOGOS ==========
    private StaffDAO staffDAO = new StaffDAO();
    private JobPositionDAO jobPositionDAO = new JobPositionDAO();
    private ShiftDAO shiftDAO = new ShiftDAO();

    private Map<Integer, String> positions;
    private Map<Integer, String> shifts;

    private Staff editingStaff;

    @FXML
    public void initialize() {
        loadCatalogs();
        setupDateFormat();
        setupSalaryFormatter();
        setupButtonActions();
    }

    // Evita que se pueda escribir un número más grande de lo que la base admite (DECIMAL(12,2))
    private void setupSalaryFormatter() {
        UnaryOperator<TextFormatter.Change> filtro = change -> {
            String textoNuevo = change.getControlNewText();
            if (textoNuevo.isEmpty() || SALARY_TYPING_PATTERN.matcher(textoNuevo).matches()) {
                return change;
            }
            return null; // rechaza el cambio: no se deja escribir ese caracter
        };
        txtSalary.setTextFormatter(new TextFormatter<>(filtro));
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
            staff.setStatus(StaffStatus.ACTIVE);
        }

        staff.setHireDate(dateHireDate.getValue());

        String shiftSelected = comboShiftName.getSelectionModel().getSelectedItem();
        staff.setIdShift((shiftSelected == null || shiftSelected.isEmpty())
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

    // ========== VALIDACIÓN COMPLETA: junta TODOS los errores antes de mostrarlos ==========
    private boolean validateFields() {
        List<String> errores = new ArrayList<>();
        limpiarEstilos();

        String firstName = txtFirstName.getText().trim();
        if (firstName.isEmpty()) {
            errores.add("El Nombre es obligatorio.");
            marcarInvalido(txtFirstName);
        } else if (!NAME_PATTERN.matcher(firstName).matches()) {
            errores.add("El Nombre solo puede contener letras y espacios.");
            marcarInvalido(txtFirstName);
        }

        String lastName = txtLastName.getText().trim();
        if (lastName.isEmpty()) {
            errores.add("El Apellido es obligatorio.");
            marcarInvalido(txtLastName);
        } else if (!NAME_PATTERN.matcher(lastName).matches()) {
            errores.add("El Apellido solo puede contener letras y espacios.");
            marcarInvalido(txtLastName);
        }

        String dni = txtDni.getText().trim();
        if (dni.isEmpty()) {
            errores.add("El DNI es obligatorio.");
            marcarInvalido(txtDni);
        } else if (!DNI_PATTERN.matcher(dni).matches()) {
            errores.add("El DNI debe contener solo números (7 a 9 dígitos).");
            marcarInvalido(txtDni);
        }

        LocalDate birthDate = dateBirthDate.getValue();
        if (birthDate == null) {
            errores.add("La Fecha de Nacimiento es obligatoria.");
            marcarInvalido(dateBirthDate);
        } else if (birthDate.plusYears(MIN_AGE_YEARS).isAfter(LocalDate.now())) {
            errores.add("El empleado debe tener al menos " + MIN_AGE_YEARS + " años.");
            marcarInvalido(dateBirthDate);
        }

        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            errores.add("El Teléfono es obligatorio.");
            marcarInvalido(txtPhone);
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            errores.add("El Teléfono solo puede contener números, espacios y guiones.");
            marcarInvalido(txtPhone);
        }

        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errores.add("El formato de Email no es válido (ejemplo: nombre@dominio.com).");
            marcarInvalido(txtEmail);
        }

        String street = txtStreet.getText().trim();
        if (street.isEmpty()) {
            errores.add("La Calle es obligatoria.");
            marcarInvalido(txtStreet);
        } else if (street.length() > MAX_STREET_LENGTH) {
            errores.add("La Calle no puede superar los " + MAX_STREET_LENGTH + " caracteres.");
            marcarInvalido(txtStreet);
        }

        if (txtAddressNumber.getText().trim().length() > MAX_ADDRESS_NUMBER_LENGTH) {
            errores.add("El Número no puede superar los " + MAX_ADDRESS_NUMBER_LENGTH + " caracteres.");
            marcarInvalido(txtAddressNumber);
        }

        String city = txtCity.getText().trim();
        if (city.isEmpty()) {
            errores.add("La Ciudad es obligatoria.");
            marcarInvalido(txtCity);
        } else if (city.length() > MAX_CITY_LENGTH) {
            errores.add("La Ciudad no puede superar los " + MAX_CITY_LENGTH + " caracteres.");
            marcarInvalido(txtCity);
        }

        if (comboPosition.getSelectionModel().isEmpty()) {
            errores.add("Debe seleccionar un Cargo.");
            marcarInvalido(comboPosition);
        }

        LocalDate hireDate = dateHireDate.getValue();
        if (hireDate == null) {
            errores.add("La Fecha de Ingreso es obligatoria.");
            marcarInvalido(dateHireDate);
        } else if (hireDate.isAfter(LocalDate.now())) {
            errores.add("La Fecha de Ingreso no puede ser una fecha futura.");
            marcarInvalido(dateHireDate);
        } else if (birthDate != null && hireDate.isBefore(birthDate.plusYears(MIN_AGE_YEARS))) {
            errores.add("La Fecha de Ingreso no puede ser anterior a que el empleado cumpliera " + MIN_AGE_YEARS + " años.");
            marcarInvalido(dateHireDate);
        }

        try {
            if (!txtShiftStart.getText().trim().isEmpty()) LocalTime.parse(txtShiftStart.getText().trim());
        } catch (DateTimeParseException e) {
            errores.add("La Hora de inicio del turno debe tener formato HH:mm (ej: 07:00).");
            marcarInvalido(txtShiftStart);
        }
        try {
            if (!txtShiftEnd.getText().trim().isEmpty()) LocalTime.parse(txtShiftEnd.getText().trim());
        } catch (DateTimeParseException e) {
            errores.add("La Hora de fin del turno debe tener formato HH:mm (ej: 15:00).");
            marcarInvalido(txtShiftEnd);
        }

        String salaryText = txtSalary.getText().trim();
        if (salaryText.isEmpty()) {
            errores.add("El Salario es obligatorio.");
            marcarInvalido(txtSalary);
        } else {
            try {
                java.math.BigDecimal salario = new java.math.BigDecimal(salaryText);
                if (salario.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    errores.add("El Salario debe ser mayor a $0.");
                    marcarInvalido(txtSalary);
                } else if (salario.compareTo(SALARY_MAX) > 0) {
                    errores.add("El Salario no puede superar $" + SALARY_MAX + ".");
                    marcarInvalido(txtSalary);
                }
            } catch (NumberFormatException e) {
                errores.add("El Salario debe ser un número válido (ejemplo: 450000).");
                marcarInvalido(txtSalary);
            }
        }

        if (!errores.isEmpty()) {
            showAlert(String.join("\n", errores));
            return false;
        }
        return true;
    }

    // Pinta el borde de un campo de rojo para señalar que tiene un error
    private void marcarInvalido(Node campo) {
        campo.setStyle(ESTILO_INVALIDO);
    }

    // Saca cualquier marca de error de todos los campos, antes de validar de nuevo
    private void limpiarEstilos() {
        for (Node campo : new Node[]{txtFirstName, txtLastName, txtDni, dateBirthDate, txtPhone,
                txtEmail, txtStreet, txtAddressNumber, txtCity, comboPosition, dateHireDate,
                txtShiftStart, txtShiftEnd, txtSalary}) {
            campo.setStyle(ESTILO_VALIDO);
        }
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