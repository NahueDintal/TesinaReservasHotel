package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.*;
import repositories.*;

import java.sql.SQLException;
import java.util.Map;

public class CustomerFormController {

    // ========== FORM COMPONENTS ==========
    @FXML private Label lblFormTitle;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private ComboBox<String> comboDocumentType;
    @FXML private TextField txtDocumentNumber;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> comboCountry;
    @FXML private ComboBox<String> comboOrigin;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ========== DAOs AND MAPS ==========
    private CustomerDAO customerDAO = new CustomerDAO();
    private DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
    private CountryDAO countryDAO = new CountryDAO();
    private CustomerStatusDAO customerStatusDAO = new CustomerStatusDAO();
    private CustomerOriginDAO customerOriginDAO = new CustomerOriginDAO();

    private Map<Integer, String> documentTypes;
    private Map<Integer, String> countries;
    private Map<Integer, String> origins;
    private Map<Integer, String> statuses;

    private Customer editingCustomer; // null if creating new
    private boolean isEditing = false;

    // ========== INITIALIZATION ==========
    @FXML
    public void initialize() {
        loadCatalogs();
        setupButtonActions();

        if (editingCustomer == null) {
            btnSave.setText("Guardar");
        }
        setMaxLength(txtFirstName, 100);
        setMaxLength(txtLastName, 100);
        setMaxLength(txtDocumentNumber, 30);
        setMaxLength(txtPhone, 30);
        setMaxLength(txtEmail, 255);
    }

    // ========== LOAD METHODS ==========
    private void loadCatalogs() {
        try {
            documentTypes = documentTypeDAO.listAll();
            comboDocumentType.getItems().setAll(documentTypes.values());

            countries = countryDAO.listAll();
            comboCountry.getItems().setAll(countries.values());

            statuses = new CustomerStatusDAO().listAll();

            origins = customerOriginDAO.listAll();
            comboOrigin.getItems().setAll(origins.values());

        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los catálogos", e.getMessage());
        }
    }

    private void setupButtonActions() {
        btnCancel.setOnAction(e -> closeWindow());
        btnSave.setOnAction(e -> saveCustomer());
    }

    // ========== PUBLIC METHOD TO SET CUSTOMER FOR EDITING ==========
    public void setCustomer(Customer customer) {
        this.editingCustomer = customer;
        this.isEditing = true;

        lblFormTitle.setText("Edite el Formulario");
        btnSave.setText("Actualizar");

        txtFirstName.setText(customer.getName());
        txtLastName.setText(customer.getSurname());
        txtDocumentNumber.setText(customer.getDocumentNumber());
        txtPhone.setText(customer.getPhoneNumber());
        txtEmail.setText(customer.getEmail());
        comboDocumentType.getSelectionModel().select(customer.getDocumentTypeName());
        comboCountry.getSelectionModel().select(customer.getCountryName());
        comboOrigin.getSelectionModel().select(customer.getOriginName());
    }

    // ========== SAVE METHODS ==========
    private void saveCustomer() {
        if (!validateFields()) return;

        Customer customer = editingCustomer != null ? editingCustomer : new Customer();
        loadDataFromForm(customer);

        try {
            // 1. Validar duplicado de documento
            int excludeId = editingCustomer != null ? editingCustomer.getIdCustomer() : 0;
            boolean isDocumentationExisting = customerDAO.isDuplicatedByDocumentation(
                    customer.getDocumentNumber(),
                    customer.getIdDocumentType(),
                    excludeId
            );

            if (isDocumentationExisting) {
                showAlert("Error", "Cliente duplicado",
                        "Ya existe un cliente con el mismo número de documento y tipo.");
                return;
            }

            // 2. Validar duplicado de teléfono
            boolean isPhoneNumberExisting = customerDAO.isDuplicatedByPhone(
                    customer.getPhoneNumber(),
                    excludeId
            );

            if (isPhoneNumberExisting) {
                showAlert("Error", "Teléfono duplicado",
                        "Ya existe un cliente con el mismo número de teléfono.");
                return;
            }

            // 3. guardar
            boolean success;
            if (editingCustomer != null) {
                success = customerDAO.isUpdate(customer);
            } else {
                success = customerDAO.isInsert(customer);
            }

            if (success) {
                showAlert("Éxito", "Cliente guardado",
                        editingCustomer != null ? "El cliente ha sido actualizado correctamente."
                                : "El cliente se ha creado correctamente.");
                closeWindow();
            }
        } catch (SQLException e) {
            showAlert("Error", "No se pudo guardar el cliente", e.getMessage());
        }
    }

    // ========== HELPER METHODS ==========
    private void loadDataFromForm(Customer customer) {
        customer.setName(txtFirstName.getText().trim());
        customer.setSurname(txtLastName.getText().trim());
        customer.setDocumentNumber(txtDocumentNumber.getText().trim());
        customer.setPhoneNumber(txtPhone.getText().trim());
        customer.setEmail(txtEmail.getText().trim());

        // Obtener IDs de los combos
        int idDocType = getIdBySelection(comboDocumentType, documentTypes);
        int idCountry = getIdBySelection(comboCountry, countries);
        int idOrigin = getIdBySelection(comboOrigin, origins);

        // Obtener ID del estado "Activo" automáticamente
        int idStatus = getActiveStatusId();

        // Asignar los IDs al cliente
        customer.setIdDocumentType(idDocType);
        customer.setIdCountry(idCountry);
        customer.setIdCustomerStatus(idStatus);
        customer.setIdCustomerOrigin(idOrigin);
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
        if (comboDocumentType.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccione un Tipo de Documento", "");
            return false;
        }
        if (txtDocumentNumber.getText().trim().isEmpty()) {
            showAlert("Validación", "El Número de Documento es obligatorio", "");
            return false;
        }
        if (comboCountry.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccione un País", "");
            return false;
        }
        if (comboOrigin.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccione un Origen", "");
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
    private int getActiveStatusId() {
        // Si statuses es null, cargarlo ahora
        if (statuses == null) {
            try {
                statuses = new CustomerStatusDAO().listAll();
            } catch (SQLException e) {
                showAlert("Error", "No se pudo cargar el estado 'Activo'", e.getMessage());
                return 1; // Valor por defecto
            }
        }

        for (Map.Entry<Integer, String> entry : statuses.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("Activo")) {
                return entry.getKey();
            }
        }
        // Si no encuentra "Activo", devuelve 1
        return 1;
    }
    private void setMaxLength(TextField field, int maxLength) {
        field.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= maxLength) {
                return change;
            }
            return null;
        }));
    }
}