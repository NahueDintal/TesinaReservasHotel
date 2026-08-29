package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.*;
import repositories.*;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Map;

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
    @FXML private TextField txtShiftName;
    @FXML private TextField txtShiftStart;   // formato "HH:mm", ej: "07:00"
    @FXML private TextField txtShiftEnd;     // formato "HH:mm", ej: "15:00"
    @FXML private TextField txtSalary;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

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
        txtShiftName.setText(staff.getShiftName());
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
        staff.setShiftName(txtShiftName.getText().trim());
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

    private boolean validateFields() {
        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
            showAlert("Validación", "Nombre y Apellido son obligatorios", "Complete los campos marcados con *");
            return false;
        }
        if (txtDni.getText().trim().isEmpty()) {
            showAlert("Validación", "El DNI es obligatorio", "");
            return false;
        }
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
        try {
            if (!txtSalary.getText().trim().isEmpty()) {
                new java.math.BigDecimal(txtSalary.getText().trim());
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