package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.*;

import java.sql.SQLException;
import java.util.Map;

public class CustomerController {  // ⬅️ Ya NO implementa Initializable

    // ========== COMPONENTES FXML ==========
    @FXML private TextField txtName;
    @FXML private TextField txtSurname;
    @FXML private ComboBox<String> comboDocumentType;
    @FXML private TextField txtDocumentNumber;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> comboCountry;
    @FXML private ComboBox<String> comboStatus;
    @FXML private ComboBox<String> comboOrigin;

    @FXML private TableView<Customer> tableCustomers;
    @FXML private TableColumn<Customer, Integer> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colSurname;
    @FXML private TableColumn<Customer, String> colDocument;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colStatus;

    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    // ========== COMPONENTES DEL PANEL DE DETALLE ==========
    @FXML private Label lblDetalleNombre;
    @FXML private Label lblDetalleEstado;
    @FXML private Label lblDetalleId;
    @FXML private Label lblDetalleDni;
    @FXML private Label lblDetalleFechaNac;
    @FXML private Label lblDetalleGenero;
    @FXML private Label lblDetalleNacionalidad;
    @FXML private Label lblDetalleCelular;
    @FXML private Label lblDetalleFijo;
    @FXML private Label lblDetalleEmail;
    @FXML private Label lblDetalleDireccion;
    @FXML private Label lblDetalleCiudad;
    @FXML private Label lblDetalleProvincia;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> comboFiltroEstado;
    @FXML private Label lblTotalClientes;

    // ========== DAOs y Mapas ==========
    private CustomerDAO customerDAO = new CustomerDAO();
    private Map<Integer, String> documentTypes;
    private Map<Integer, String> countries;
    private Map<Integer, String> statuses;
    private Map<Integer, String> origins;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    // ========== INICIALIZACIÓN (ahora con @FXML) ==========
    @FXML
    public void initialize() {  // ⬅️ Sin parámetros y con @FXML
        // 1. Configurar columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("idCustomer"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colDocument.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusName")); // Asumiendo que tenés este campo

        // 2. Cargar datos de catálogos
        loadCatalogs();

        // 3. Cargar lista de clientes
        loadCustomers();

        // 4. Escuchar selección en la tabla para actualizar el detalle
        tableCustomers.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showDetail(newSelection);
                    } else {
                        clearDetail();
                    }
                }
        );

        // 5. Deshabilitar botones si no hay selección
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        tableCustomers.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    boolean isSelected = newVal != null;
                    btnEdit.setDisable(!isSelected);
                    btnDelete.setDisable(!isSelected);
                }
        );
    }

    // ========== MÉTODOS DE CARGA ==========
    private void loadCatalogs() {
        try {
            documentTypes = new DocumentTypeDAO().listAll();
            comboDocumentType.getItems().setAll(documentTypes.values());

            countries = new CountryDAO().listAll();
            comboCountry.getItems().setAll(countries.values());

            statuses = new CustomerStatusDAO().listAll();
            comboStatus.getItems().setAll(statuses.values());

            origins = new CustomerOriginDAO().listAll();
            comboOrigin.getItems().setAll(origins.values());

        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los catálogos", e.getMessage());
        }
    }

    private void loadCustomers() {
        try {
            customerList.setAll(customerDAO.listAll());
            tableCustomers.setItems(customerList);
            updateCounter();
        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los clientes", e.getMessage());
        }
    }

    // ========== MÉTODOS DEL PANEL DE DETALLE ==========
    private void showDetail(Customer customer) {
        lblDetalleNombre.setText(customer.getName() + " " + customer.getSurname());
        lblDetalleEstado.setText(customer.getStatusName() != null ? customer.getStatusName() : "Sin estado");
        lblDetalleId.setText("ID: " + customer.getIdCustomer());
        lblDetalleDni.setText(customer.getDocumentNumber());
        // Los siguientes campos necesitan que los agregues en la clase Customer
        lblDetalleFechaNac.setText("--"); // Podés agregar fechaNacimiento a Customer
        lblDetalleGenero.setText("--");
        lblDetalleNacionalidad.setText(customer.getCountryName() != null ? customer.getCountryName() : "--");
        lblDetalleCelular.setText(customer.getPhoneNumber());
        lblDetalleFijo.setText("--");
        lblDetalleEmail.setText(customer.getEmail());
        lblDetalleDireccion.setText("--");
        lblDetalleCiudad.setText("--");
        lblDetalleProvincia.setText("--");
    }

    private void clearDetail() {
        lblDetalleNombre.setText("Seleccione un cliente");
        lblDetalleEstado.setText("");
        lblDetalleId.setText("");
        lblDetalleDni.setText("");
        lblDetalleFechaNac.setText("");
        lblDetalleGenero.setText("");
        lblDetalleNacionalidad.setText("");
        lblDetalleCelular.setText("");
        lblDetalleFijo.setText("");
        lblDetalleEmail.setText("");
        lblDetalleDireccion.setText("");
        lblDetalleCiudad.setText("");
        lblDetalleProvincia.setText("");
    }

    // ========== MÉTODOS CRUD ==========
    @FXML
    private void addCustomer() {
        if (!validateFields()) return;

        Customer nuevo = new Customer();
        loadDataFromForm(nuevo);

        try {
            if (customerDAO.isInsert(nuevo)) {
                customerList.add(nuevo);
                showAlert("Éxito", "Cliente agregado", "ID: " + nuevo.getIdCustomer());
                clearForm();
                updateCounter();
            }
        } catch (SQLException e) {
            showAlert("Error", "No se pudo agregar", e.getMessage());
        }
    }

    @FXML
    private void editCustomer() {
        Customer selected = tableCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!validateFields()) return;

        loadDataFromForm(selected);

        try {
            if (customerDAO.isUpdate(selected)) {
                tableCustomers.refresh();
                showAlert("Éxito", "Cliente actualizado", "ID: " + selected.getIdCustomer());
                clearForm();
                showDetail(selected);
            }
        } catch (SQLException e) {
            showAlert("Error", "No se pudo actualizar", e.getMessage());
        }
    }

    @FXML
    private void deleteCustomer() {
        Customer selected = tableCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar cliente?");
        alert.setContentText("¿Estás seguro de eliminar a " + selected.getName() + " " + selected.getSurname() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (customerDAO.isDelete(selected.getIdCustomer())) {
                        customerList.remove(selected);
                        showAlert("Éxito", "Cliente eliminado", "");
                        clearForm();
                        clearDetail();
                        updateCounter();
                    }
                } catch (SQLException e) {
                    showAlert("Error", "No se pudo eliminar", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void clearFields() {
        clearForm();
    }

    // ========== MÉTODOS AUXILIARES ==========
    private void loadDataFromForm(Customer customer) {
        customer.setName(txtName.getText().trim());
        customer.setSurname(txtSurname.getText().trim());
        customer.setDocumentNumber(txtDocumentNumber.getText().trim());
        customer.setPhoneNumber(txtPhone.getText().trim());
        customer.setEmail(txtEmail.getText().trim());

        customer.setIdDocumentType(getIdBySelection(comboDocumentType, documentTypes));
        customer.setIdCountry(getIdBySelection(comboCountry, countries));
        customer.setIdCustomerStatus(getIdBySelection(comboStatus, statuses));
        customer.setIdCustomerOrigin(getIdBySelection(comboOrigin, origins));
    }

    private void clearForm() {
        txtName.clear();
        txtSurname.clear();
        txtDocumentNumber.clear();
        txtPhone.clear();
        txtEmail.clear();
        comboDocumentType.getSelectionModel().clearSelection();
        comboCountry.getSelectionModel().clearSelection();
        comboStatus.getSelectionModel().clearSelection();
        comboOrigin.getSelectionModel().clearSelection();
        tableCustomers.getSelectionModel().clearSelection();
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
        if (txtName.getText().trim().isEmpty() || txtSurname.getText().trim().isEmpty()) {
            showAlert("Validación", "Nombre y Apellido son obligatorios", "");
            return false;
        }
        if (comboDocumentType.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccioná un Tipo de Documento", "");
            return false;
        }
        if (txtDocumentNumber.getText().trim().isEmpty()) {
            showAlert("Validación", "El Nº de Documento es obligatorio", "");
            return false;
        }
        if (comboCountry.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccioná un País", "");
            return false;
        }
        if (comboStatus.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccioná un Estado", "");
            return false;
        }
        if (comboOrigin.getSelectionModel().isEmpty()) {
            showAlert("Validación", "Seleccioná un Origen", "");
            return false;
        }
        return true;
    }

    private void updateCounter() {
        lblTotalClientes.setText("Mostrando " + customerList.size() + " clientes");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}