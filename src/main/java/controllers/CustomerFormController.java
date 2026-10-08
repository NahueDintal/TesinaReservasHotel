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
    @FXML private TextField txtName;
    @FXML private TextField txtSurname;
    @FXML private ComboBox<String> comboDocumentType;
    @FXML private TextField txtDocumentNumber;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> comboCountry;
    @FXML private ComboBox<String> comboProvince;
    @FXML private ComboBox<String> comboOrigin;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ========== CAMPOS QUE YA FUERON TOCADOS POR EL USUARIO ==========
    private Set<Control> touchedFields = new HashSet<>();

    // ========== DAOs AND MAPS ==========
    private CustomerDAO customerDAO = new CustomerDAO();
    private DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
    private CountryDAO countryDAO = new CountryDAO();
    private ProvinceDAO provinceDAO = new ProvinceDAO();
    private CustomerStatusDAO customerStatusDAO = new CustomerStatusDAO();
    private CustomerOriginDAO customerOriginDAO = new CustomerOriginDAO();

    private Map<Integer, String> documentTypes;
    private Map<Integer, String> countries;
    private Map<Integer, String> provinces;
    private Map<Integer, String> statuses;
    private Map<Integer, String> origins;

    private Customer editingCustomer;
    private static final int ARGENTINA_ID = 9;

    // ========== INITIALIZATION ==========
    @FXML
    public void initialize() {
        loadCatalogs();
        setupButtonActions();
        setupValidations();

        comboProvince.setDisable(true);

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

            provinces = provinceDAO.listAll();
            comboProvince.getItems().setAll(provinces.values());

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
        txtName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                touchedFields.add(txtName);
                validateName();
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
            if (newVal != null) {
                // Obtener el ID del país seleccionado
                int countryId = getIdBySelection(comboCountry, countries);
                boolean isArgentina = (countryId == ARGENTINA_ID);

                // Habilitar/deshabilitar provincia según el país
                comboProvince.setDisable(!isArgentina);

                // Si NO es Argentina, limpiar la provincia
                if (!isArgentina) {
                    comboProvince.getSelectionModel().clearSelection();
                }

                // Actualizar el estado del botón guardar
                updateSaveButtonState();
            }
        });

        //revisar
        comboProvince.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) updateSaveButtonState();
        });

        comboOrigin.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) updateSaveButtonState();
        });


        // Validar txts mientras se escribe (sin feedback visual)
        txtName.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 50) {
                txtName.setText(oldVal);
            }
            updateSaveButtonState();
        });
        txtSurname.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 50) {
                txtSurname.setText(oldVal);
            }
            updateSaveButtonState();
        });
        txtDocumentNumber.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 30) {
                txtDocumentNumber.setText(oldVal);
            }
            updateSaveButtonState();
        });
        txtPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 30) {
                txtPhone.setText(oldVal);
            }
            updateSaveButtonState();
        });
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 255) {
                txtEmail.setText(oldVal);
            }
            updateSaveButtonState();
        });
    }

    private void updateDocumentValidation(String documentType) {
        // Actualizar la validación del documento según el tipo
        validateDocument();
    }

    // ========== VALIDACIONES INDIVIDUALES ==========
    private boolean validateName() {
        String name = txtName.getText();
        boolean valid = ValidationUtils.isValidName(name);
        System.out.println("🔍 validateName() - Texto: '" + name + "' | Válido: " + valid);
        setFieldValid(txtName, valid, ValidationUtils.getNameError());
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

    // ========== HELPER: MARCAR CAMPO VÁLIDO/INVÁLIDO ==========
    private void setFieldValid(Control field, boolean valid, String errorMessage) {
        // Solo mostrar feedback visual si el campo ya fue tocada una vez
        if (!touchedFields.contains(field)) {
            return; // ← Salir sin hacer nada
        }

        System.out.println("🔍 Validando campo: " + field.getId() +
                " | Válido: " + valid +
                " | Mensaje: " + errorMessage);

        if (valid) {
            field.setStyle("");
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
        // Validar campos comunes
        boolean nameValid = ValidationUtils.isValidName(txtName.getText());
        boolean surnameValid = ValidationUtils.isValidName(txtSurname.getText());
        boolean documentValid = ValidationUtils.isValidDocument(
                txtDocumentNumber.getText(),
                comboDocumentType.getSelectionModel().getSelectedItem()
        );
        boolean phoneValid = ValidationUtils.isValidPhone(txtPhone.getText());
        boolean emailValid = ValidationUtils.isValidEmail(txtEmail.getText());

        // Validar combos obligatorios
        boolean docTypeSelected = !comboDocumentType.getSelectionModel().isEmpty();
        boolean countrySelected = !comboCountry.getSelectionModel().isEmpty();
        boolean originSelected = !comboOrigin.getSelectionModel().isEmpty();

        // Validar provincia SOLO si el país es Argentina
        int countryId = getIdBySelection(comboCountry, countries);
        boolean isArgentina = (countryId == ARGENTINA_ID);
        boolean provinceValid = !isArgentina || !comboProvince.getSelectionModel().isEmpty();

        boolean allValid = nameValid && surnameValid && documentValid &&
                phoneValid && emailValid &&
                docTypeSelected && countrySelected && originSelected &&
                provinceValid;

        btnSave.setDisable(!allValid);
    }

    // ========== BOTONES ==========
    private void setupButtonActions() {
        btnCancel.setOnAction(e -> {
            boolean confirmed = StyleManager.showConfirmation(
                    "Cancelar",
                    "¿Descartar cambios?",
                    "Perdera todo lo que haya modificado o agregado en el formulario"
            );
            if (confirmed) {
                closeWindow();
            }
        });
        btnSave.setOnAction(e -> saveCustomer());
    }

    // ========== SET CUSTOMER (para edición) ==========
    public void setCustomer(Customer customer) {
        this.editingCustomer = customer;
        btnSave.setText("Actualizar");

        txtName.setText(customer.getName());
        txtSurname.setText(customer.getSurname());
        txtDocumentNumber.setText(customer.getDocumentNumber());
        txtPhone.setText(customer.getPhoneNumber());
        txtEmail.setText(customer.getEmail());

        comboDocumentType.getSelectionModel().select(customer.getDocumentTypeName());
        comboCountry.getSelectionModel().select(customer.getCountryName());
        comboOrigin.getSelectionModel().select(customer.getOriginName());

        // Si el cliente es de Argentina, seleccionar la provincia
        if (customer.getIdCountry() == ARGENTINA_ID && customer.getIdProvince() > 0) {
            comboProvince.getSelectionModel().select(customer.getProvinceName());
        }

        // Validar todos los campos después de cargar
        validateName();
        validateSurname();
        validateDocument();
        validatePhone();
        validateEmail();
        updateSaveButtonState();
    }

    // ========== SAVE ==========
    private void saveCustomer() {

        if (!validateAllFields()) {
            return;
        }

        Customer customer = editingCustomer != null ? editingCustomer : new Customer();
        loadDataFromForm(customer);

        try {
            // Validar duplicados
            int excludeId = editingCustomer != null ? editingCustomer.getIdCustomer() : 0;

            if (customerDAO.isDuplicatedByDocumentation(customer.getDocumentNumber(),
                    customer.getIdDocumentType(), excludeId)) {
                showAlert("Error", "Cliente duplicado",
                        "Ya existe un cliente con el mismo número de documento y tipo.");
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
        // Marcar TODOS los campos como tocados
        touchedFields.add(txtName);
        touchedFields.add(txtSurname);
        touchedFields.add(txtDocumentNumber);
        touchedFields.add(txtPhone);
        touchedFields.add(txtEmail);
        touchedFields.add(comboDocumentType);
        touchedFields.add(comboCountry);
        touchedFields.add(comboProvince);
        touchedFields.add(comboOrigin);

        // Validar campos de texto
        boolean nameValid = validateName();
        boolean surnameValid = validateSurname();
        boolean documentValid = validateDocument();
        boolean phoneValid = validatePhone();
        boolean emailValid = validateEmail();

        // Validar combos obligatorios
        boolean docTypeValid = !comboDocumentType.getSelectionModel().isEmpty();
        boolean countryValid = !comboCountry.getSelectionModel().isEmpty();
        boolean originValid = !comboOrigin.getSelectionModel().isEmpty();

        // Validar provincia SOLO si el país es Argentina
        int countryId = getIdBySelection(comboCountry, countries);
        boolean isArgentina = (countryId == ARGENTINA_ID);
        boolean provinceValid = !isArgentina || !comboProvince.getSelectionModel().isEmpty();

        return nameValid && surnameValid && documentValid &&
                phoneValid && emailValid &&
                docTypeValid && countryValid && originValid &&
                provinceValid;
    }

    // ========== LOAD DATA FROM FORM ==========
    private void loadDataFromForm(Customer customer) {
        // Obtener IDs de los combos
        int idDocType = getIdBySelection(comboDocumentType, documentTypes);
        int idCountry = getIdBySelection(comboCountry, countries);
        int idOrigin = getIdBySelection(comboOrigin, origins);
        int idStatus = getActiveStatusId();

        // Asignar los IDs al customer
        customer.setIdDocumentType(idDocType);
        customer.setIdCountry(idCountry);
        customer.setIdCustomerStatus(idStatus);
        customer.setIdCustomerOrigin(idOrigin);

        // Provincia: solo si es Argentina
        boolean isArgentina = (idCountry == ARGENTINA_ID);
        if (isArgentina) {
            int idProvince = getIdBySelection(comboProvince, provinces);
            customer.setIdProvince(idProvince);
        } else {
            // Si NO es Argentina, seteamos 0 (que el DAO convierte a NULL)
            customer.setIdProvince(0);
        }

        // Campos de texto (con limpieza)
        customer.setName(txtName.getText().trim());
        customer.setSurname(txtSurname.getText().trim());
        customer.setEmail(txtEmail.getText().trim());

        // Limpiar teléfono
        String rawPhone = txtPhone.getText().trim();
        String cleanPhone = rawPhone.replaceAll("[^+0-9]", "");
        customer.setPhoneNumber(cleanPhone);

        // Limpiar documento según tipo
        String docType = comboDocumentType.getSelectionModel().getSelectedItem();
        String rawDocumentNumber = txtDocumentNumber.getText().trim();
        String cleanDocumentNumber;
        switch (docType) {
            case "dni":
                cleanDocumentNumber = rawDocumentNumber.replaceAll("[^0-9]", "");
                break;
            case "pasaporte":
                cleanDocumentNumber = rawDocumentNumber.replaceAll("[^A-Za-z0-9]", "");
                break;
            case "cedula extranjera":
                cleanDocumentNumber = rawDocumentNumber.replaceAll("[^A-Za-z0-9]", "");
                break;
            case "otro":
                cleanDocumentNumber = rawDocumentNumber.replaceAll("[^A-Za-z0-9]", "");
                break;
            default:
                cleanDocumentNumber = rawDocumentNumber.replaceAll("[^A-Za-z0-9]", "");
                break;
        }
        customer.setDocumentNumber(cleanDocumentNumber);
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
        StyleManager.applyStyles(alert.getDialogPane());
        alert.showAndWait();
    }
}