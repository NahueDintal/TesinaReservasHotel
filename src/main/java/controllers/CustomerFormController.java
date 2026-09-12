package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.*;
import repositories.*;
import utils.StyleManager;
import utils.ValidationUtils;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;


public class CustomerFormController {

    // ========== FORM COMPONENTS ==========
    @FXML private Label lblFormTitle;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtSurname;
    @FXML private ComboBox<String> comboDocumentType;
    @FXML private TextField txtDocumentNumber;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> comboCountry;
    @FXML private ComboBox<String> comboOrigin;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ========== CAMPOS QUE YA FUERON TOCADOS POR EL USUARIO ==========
    private Set<Control> touchedFields = new HashSet<>();

    // ========== DAOs AND MAPS ==========
    private CustomerDAO customerDAO = new CustomerDAO();
    private DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
    private CountryDAO countryDAO = new CountryDAO();
    private CustomerStatusDAO customerStatusDAO = new CustomerStatusDAO();
    private CustomerOriginDAO customerOriginDAO = new CustomerOriginDAO();

    private Map<Integer, String> documentTypes;
    private Map<Integer, String> countries;
    private Map<Integer, String> statuses;
    private Map<Integer, String> origins;

    private Customer editingCustomer;

    // ========== INITIALIZATION ==========
    @FXML
    public void initialize() {
        loadCatalogs();
        setupButtonActions();
        setupValidations();

        if (editingCustomer == null) {
            btnSave.setText("Guardar");
        }
    }

    // ========== LOAD CATALOGS ==========
    private void loadCatalogs() {
        try {
            documentTypes = documentTypeDAO.listAll();
            comboDocumentType.getItems().setAll(documentTypes.values());

            countries = countryDAO.listAll();
            comboCountry.getItems().setAll(countries.values());

            statuses = customerStatusDAO.listAll();

            origins = customerOriginDAO.listAll();
            comboOrigin.getItems().setAll(origins.values());

            // Seleccionar DNI por defecto
            comboDocumentType.getSelectionModel().select("dni");
            updateDocumentValidation("dni");

        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los catálogos", e.getMessage());
        }
    }

    // ========== SETUP VALIDATIONS ==========
    private void setupValidations() {
        // Validar nombres al perder el foco
        txtFirstName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                touchedFields.add(txtFirstName);
                validateFirstName();
            }
        });

        // Validar apellidos al perder el foco
        txtSurname.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                touchedFields.add(txtSurname);
                validateSurname();
            }
        });

        // Validar documento al perder el foco
        txtDocumentNumber.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                touchedFields.add(txtDocumentNumber);
                validateDocument();
            }
        });

        // Validar teléfono al perder el foco
        txtPhone.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                touchedFields.add(txtPhone);
                validatePhone();
            }
        });

        // Validar email al perder el foco
        txtEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                touchedFields.add(txtEmail);
                validateEmail();
            }
        });

        // Validar combos al seleccionar
        comboDocumentType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDocumentValidation(newVal);
                validateDocument();
            }
        });

        comboCountry.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) validateCombo(comboCountry, newVal);
        });

        comboOrigin.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) validateCombo(comboOrigin, newVal);
        });

        // Validación mientras se escribe (sin feedback visual)
        txtFirstName.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        txtSurname.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        txtDocumentNumber.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        txtPhone.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
    }

    private void updateDocumentValidation(String documentType) {
        // Actualizar la validación del documento según el tipo
        validateDocument();
    }

    // ========== VALIDACIONES INDIVIDUALES ==========
    private boolean validateFirstName() {
        String name = txtFirstName.getText();
        boolean valid = ValidationUtils.isValidName(name);
        System.out.println("🔍 validateFirstName() - Texto: '" + name + "' | Válido: " + valid);
        setFieldValid(txtFirstName, valid, ValidationUtils.getNameError());
        updateSaveButtonState();
        return valid;
    }

    private boolean validateSurname() {
        String surname = txtSurname.getText();
        boolean valid = ValidationUtils.isValidName(surname);
        System.out.println("🔍 validateSurName() - Texto: '" + surname + "' | Válido: " + valid);
        setFieldValid(txtSurname, valid, ValidationUtils.getNameError());
        updateSaveButtonState();
        return valid;
    }

    private boolean validateDocument() {
        String document = txtDocumentNumber.getText();
        String type = comboDocumentType.getSelectionModel().getSelectedItem();
        boolean valid = ValidationUtils.isValidDocument(document, type);
        System.out.println("🔍 validateDocumentNumber() - Texto: '" + document + "' | Válido: " + valid);
        setFieldValid(txtDocumentNumber, valid, ValidationUtils.getDocumentError(type));
        updateSaveButtonState();
        return valid;
    }

    private boolean validatePhone() {
        String phone = txtPhone.getText();
        boolean valid = ValidationUtils.isValidPhone(phone);
        System.out.println("🔍 validatePhoneNumber() - Texto: '" + phone + "' | Válido: " + valid);
        setFieldValid(txtPhone, valid, ValidationUtils.getPhoneError());
        updateSaveButtonState();
        return valid;
    }

    private boolean validateEmail() {
        String email = txtEmail.getText();
        boolean valid = ValidationUtils.isValidEmail(email);
        System.out.println("🔍 validateEmail() - Texto: '" + email + "' | Válido: " + valid);
        setFieldValid(txtEmail, valid, ValidationUtils.getEmailError());
        updateSaveButtonState();
        return valid;
    }

    private void validateCombo(ComboBox<String> combo, String value) {
        boolean valid = value != null && !value.isEmpty();
        setFieldValid(combo, valid, "Este campo es obligatorio.");
        updateSaveButtonState();
    }

    // ========== HELPER: MARCAR CAMPO VÁLIDO/INVÁLIDO ==========
    private void setFieldValid(Control field, boolean valid, String errorMessage) {
        // Si el campo NO fue tocado por el usuario, no mostrar feedback visual
        if (!touchedFields.contains(field)) {
            return; // ← Salir sin hacer nada
        }

        System.out.println("🔍 Validando campo: " + field.getId() +
                " | Válido: " + valid +
                " | Mensaje: " + errorMessage);

        if (valid) {
            field.setStyle(""); // Limpiar borde rojo
            field.setTooltip(null);
            System.out.println("   ✅ Campo válido, borde limpiado");
        } else {
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
            Tooltip tooltip = new Tooltip(errorMessage);
            field.setTooltip(tooltip);
            System.out.println("   ❌ Campo inválido, borde rojo aplicado");
        }
    }

    // ========== ACTUALIZAR ESTADO DEL BOTÓN GUARDAR ==========
    private void updateSaveButtonState() {
        boolean allValid = ValidationUtils.isValidName(txtFirstName.getText()) &&
                ValidationUtils.isValidName(txtSurname.getText()) &&
                ValidationUtils.isValidDocument(txtDocumentNumber.getText(),
                        comboDocumentType.getSelectionModel().getSelectedItem()) &&
                ValidationUtils.isValidPhone(txtPhone.getText()) &&
                ValidationUtils.isValidEmail(txtEmail.getText()) &&
                !comboDocumentType.getSelectionModel().isEmpty() &&
                !comboCountry.getSelectionModel().isEmpty() &&
                !comboOrigin.getSelectionModel().isEmpty();

        btnSave.setDisable(!allValid);
    }

    // ========== BOTONES ==========
    private void setupButtonActions() {
        btnCancel.setOnAction(e -> closeWindow());
        btnSave.setOnAction(e -> saveCustomer());
    }

    // ========== SET CUSTOMER (para edición) ==========
    public void setCustomer(Customer customer) {
        this.editingCustomer = customer;
        btnSave.setText("Actualizar");

        txtFirstName.setText(customer.getName());
        txtSurname.setText(customer.getSurname());
        txtDocumentNumber.setText(customer.getDocumentNumber());
        txtPhone.setText(customer.getPhoneNumber());
        txtEmail.setText(customer.getEmail());

        comboDocumentType.getSelectionModel().select(customer.getDocumentTypeName());
        comboCountry.getSelectionModel().select(customer.getCountryName());
        comboOrigin.getSelectionModel().select(customer.getOriginName());

        // Validar todos los campos después de cargar
        validateFirstName();
        validateSurname();
        validateDocument();
        validatePhone();
        validateEmail();
        updateSaveButtonState();
    }

    // ========== SAVE ==========
    private void saveCustomer() {
        // Validar el campo activo antes de guardar
        if (!validateAllFields()) {
            return;
        }

        Customer customer = editingCustomer != null ? editingCustomer : new Customer();
        loadDataFromForm(customer);

        try {
            // Validar duplicados (si es necesario)
            int excludeId = editingCustomer != null ? editingCustomer.getIdCustomer() : 0;

            if (customerDAO.isDuplicatedByDocumentation(customer.getDocumentNumber(),
                    customer.getIdDocumentType(), excludeId)) {
                showAlert("Error", "Cliente duplicado",
                        "Ya existe un cliente con el mismo número de documento y tipo.");
                return;
            }

            if (customerDAO.isDuplicatedByPhone(customer.getPhoneNumber(), excludeId)) {
                showAlert("Error", "Teléfono duplicado",
                        "Ya existe un cliente con el mismo número de teléfono.");
                return;
            }

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

    private boolean validateAllFields() {
        // Marcar TODOS los campos como tocados al intentar guardar
        touchedFields.add(txtFirstName);
        touchedFields.add(txtSurname);
        touchedFields.add(txtDocumentNumber);
        touchedFields.add(txtPhone);
        touchedFields.add(txtEmail);
        touchedFields.add(comboDocumentType);
        touchedFields.add(comboCountry);
        touchedFields.add(comboOrigin);

        boolean firstNameValid = validateFirstName();
        boolean surnameValid = validateSurname();
        boolean documentValid = validateDocument();
        boolean phoneValid = validatePhone();
        boolean emailValid = validateEmail();

        // Validar combos
        boolean docTypeValid = !comboDocumentType.getSelectionModel().isEmpty();
        boolean countryValid = !comboCountry.getSelectionModel().isEmpty();
        boolean originValid = !comboOrigin.getSelectionModel().isEmpty();

        if (!docTypeValid) setFieldValid(comboDocumentType, false, "Seleccione un tipo de documento.");
        if (!countryValid) setFieldValid(comboCountry, false, "Seleccione un país.");
        if (!originValid) setFieldValid(comboOrigin, false, "Seleccione un origen.");

        return firstNameValid && surnameValid && documentValid &&
                phoneValid && emailValid && docTypeValid && countryValid && originValid;
    }

    // ========== LOAD DATA FROM FORM ==========
    private void loadDataFromForm(Customer customer) {
        customer.setName(txtFirstName.getText().trim());
        customer.setSurname(txtSurname.getText().trim());
        customer.setDocumentNumber(txtDocumentNumber.getText().trim());
        customer.setEmail(txtEmail.getText().trim());

        // Limpiar el teléfono antes de guardar
        String rawPhone = txtPhone.getText().trim();
        String cleanPhone = rawPhone.replaceAll("[^+0-9]", "");
        customer.setPhoneNumber(cleanPhone);

        int idDocType = getIdBySelection(comboDocumentType, documentTypes);
        int idCountry = getIdBySelection(comboCountry, countries);
        int idOrigin = getIdBySelection(comboOrigin, origins);
        int idStatus = getActiveStatusId();

        customer.setIdDocumentType(idDocType);
        customer.setIdCountry(idCountry);
        customer.setIdCustomerStatus(idStatus);
        customer.setIdCustomerOrigin(idOrigin);
    }

    private int getIdBySelection(ComboBox<String> combo, Map<Integer, String> map) {
        String selected = combo.getSelectionModel().getSelectedItem();
        if (selected == null) return 0;
        selected = selected.trim();

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().trim().equalsIgnoreCase(selected)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    private int getActiveStatusId() {
        if (statuses == null) {
            try {
                statuses = new CustomerStatusDAO().listAll();
            } catch (SQLException e) {
                showAlert("Error", "No se pudo cargar el estado 'Activo'", e.getMessage());
                return 1;
            }
        }

        for (Map.Entry<Integer, String> entry : statuses.entrySet()) {
            if (entry.getValue().equalsIgnoreCase("active")) {
                return entry.getKey();
            }
        }
        return 1;
    }

    // ========== HELPER ==========
    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Aplicar el CSS global a la alerta
        StyleManager.applyStyles(alert.getDialogPane());

        alert.showAndWait();
    }
}