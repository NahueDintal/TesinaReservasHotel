package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import java.util.Map;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.*;
import repositories.*;
import utils.StyleManager;

import java.sql.SQLException;

public class InactiveCustomersController {

    // ========== TABLE COMPONENTS ==========
    @FXML private TableView<Customer> tableInactiveCustomers;
    @FXML private TableColumn<Customer, String> colFirstName;
    @FXML private TableColumn<Customer, String> colSurname;
    @FXML private TableColumn<Customer, String> colDocumentType;
    @FXML private TableColumn<Customer, String> colOrigin;
    @FXML private TableColumn<Customer, String> colCountry;

    @FXML private Button btnReactivate;
    @FXML private TextField txtSearch;
    @FXML private Label lblTotalInactive;

    // ========== DAOs ==========
    private CustomerDAO customerDAO = new CustomerDAO();
    private ObservableList<Customer> inactiveCustomers = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredInactive;

    // ========== INITIALIZATION ==========
    @FXML
    public void initialize() {
        // 1. Configure table columns
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colDocumentType.setCellValueFactory(new PropertyValueFactory<>("documentTypeName"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("originName"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("countryName"));

        // 2. Load inactive customers
        loadInactiveCustomers();

        // 3. Setup search filter
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.trim().isEmpty()) {
                    loadInactiveCustomers(); // Carga los últimos 100 inactivos
                } else {
                    inactiveCustomers.setAll(customerDAO.searchInactiveCustomers(newValue.trim()));
                    filteredInactive = new FilteredList<>(inactiveCustomers, p -> true);
                    tableInactiveCustomers.setItems(filteredInactive);
                    tableInactiveCustomers.refresh();
                    updateCounter();
                }
            } catch (SQLException e) {
                showAlert("Error", "No se pudo realizar la búsqueda", e.getMessage());
            }
        });

        // 4. Enable/disable reactivate button based on selection
        btnReactivate.setDisable(true);
        tableInactiveCustomers.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> btnReactivate.setDisable(newVal == null)
        );

        // 5. Action listeners
        btnReactivate.setOnAction(e -> reactivateCustomer());
    }

    // ========== LOAD METHODS ==========
    private void loadInactiveCustomers() {
        try {
            inactiveCustomers.setAll(customerDAO.listAllInactive()); // ← Usar el nuevo método
            filteredInactive = new FilteredList<>(inactiveCustomers, p -> true);
            tableInactiveCustomers.setItems(filteredInactive);
            updateCounter();
        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los clientes inactivos", e.getMessage());
        }
    }

    // ========== REACTIVATE METHOD ==========
    private void reactivateCustomer() {
        Customer selected = tableInactiveCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean confirmed = StyleManager.showConfirmation(
                "Reactivar cliente",
                "¿Desea reactivar este cliente?",
                "El cliente " + selected.getName() + " " + selected.getSurname() +
                        " volverá a estar activo y podrá realizar reservas."
        );

        if (confirmed) {
            try {
                selected.setIdCustomerStatus(getStatusIdByName("active"));
                if (customerDAO.isUpdate(selected)) {
                    inactiveCustomers.remove(selected);
                    filteredInactive.remove(selected);
                    tableInactiveCustomers.refresh();
                    updateCounter();
                    showAlert("Éxito", "Cliente reactivado",
                            "El cliente ha sido reactivado correctamente.");
                    // ✅ Cerrar la ventana SIN botón
                    Stage stage = (Stage) tableInactiveCustomers.getScene().getWindow();
                    stage.close();
                }
            } catch (SQLException e) {
                showAlert("Error", "No se pudo reactivar el cliente", e.getMessage());
            }
        }
    }

    // ========== HELPER METHODS ==========
    private int getStatusIdByName(String statusName) {
        try {
            CustomerStatusDAO statusDAO = new CustomerStatusDAO();
            Map<Integer, String> statuses = statusDAO.listAll();
            for (Map.Entry<Integer, String> entry : statuses.entrySet()) {
                if (entry.getValue().equals(statusName)) {
                    return entry.getKey();
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "No se pudo obtener el ID del estado", e.getMessage());
        }
        return 1; // Default to "Activo" if not found
    }

    private void updateCounter() {
        int count = filteredInactive != null ? filteredInactive.size() : 0;
        lblTotalInactive.setText("Mostrando " + count + " clientes inactivos");
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