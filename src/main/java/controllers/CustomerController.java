package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.*;
import repositories.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CustomerController {

    // ========== TABLE ==========
    @FXML private TableView<Customer> tableCustomers;
    @FXML private TableColumn<Customer, String> colFirstName;
    @FXML private TableColumn<Customer, String> colLastName;
    @FXML private TableColumn<Customer, String> colDocumentType;
    @FXML private TableColumn<Customer, String> colOrigin;
    @FXML private TableColumn<Customer, String> colCountry;

    // ========== BUTTONS ==========
    @FXML private Button btnNewCustomer;
    @FXML private Button btnViewInactive;
    @FXML private Button btnEdit;
    @FXML private Button btnDeactivate;

    // ========== DETAIL ==========
    @FXML private Label lblDetailFullName;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailDocumentType;
    @FXML private Label lblDetailDocumentNumber;
    @FXML private Label lblDetailPhone;
    @FXML private Label lblDetailEmail;
    @FXML private Label lblDetailCountry;
    @FXML private Label lblDetailOrigin;

    // ========== SEARCH ==========
    @FXML private TextField txtSearch;
    @FXML private Label lblTotalCustomers;

    // ========== DAOs ==========
    private CustomerDAO customerDAO = new CustomerDAO();
    private ObservableList<Customer> masterCustomerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredCustomers;

    // ========== INIT ==========
    @FXML
    public void initialize() {
        // Configurar columnas
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colDocumentType.setCellValueFactory(new PropertyValueFactory<>("documentTypeName"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("originName"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("countryName"));

        // Cargar clientes activos
        loadActiveCustomers();

        // Configurar buscador
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCustomers.setPredicate(customer -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return customer.getName().toLowerCase().contains(lower) ||
                        customer.getSurname().toLowerCase().contains(lower) ||
                        customer.getEmail().toLowerCase().contains(lower) ||
                        customer.getPhoneNumber().toLowerCase().contains(lower) ||
                        customer.getDocumentNumber().toLowerCase().contains(lower) ||
                        customer.getDocumentTypeName().toLowerCase().contains(lower) ||
                        customer.getCountryName().toLowerCase().contains(lower) ||
                        customer.getOriginName().toLowerCase().contains(lower);
            });
            updateCounter();
        });

        // Selección en tabla
        tableCustomers.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null) {
                        showDetail(newVal);
                    } else {
                        clearDetail();
                    }
                }
        );

        // Botones
        btnEdit.setDisable(true);
        btnDeactivate.setDisable(true);
        tableCustomers.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    boolean selected = newVal != null;
                    btnEdit.setDisable(!selected);
                    btnDeactivate.setDisable(!selected);
                }
        );

        // Acciones
        btnNewCustomer.setOnAction(e -> openCustomerForm(null));
        btnViewInactive.setOnAction(e -> openInactiveCustomersWindow());
        btnEdit.setOnAction(e -> openCustomerForm(tableCustomers.getSelectionModel().getSelectedItem()));
        btnDeactivate.setOnAction(e -> deactivateCustomer());
    }

    // ========== LOAD ==========
    private void loadActiveCustomers() {
        try {
            masterCustomerList.setAll(customerDAO.listAll());
            masterCustomerList.removeIf(c -> !"active".equals(c.getStatusName()));
            filteredCustomers = new FilteredList<>(masterCustomerList, p -> true);
            tableCustomers.setItems(filteredCustomers);
            //tableCustomers.refresh();
            updateCounter();
        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los clientes", e.getMessage());
        }
    }

    // ========== CLEAR FILTERS ==========
    @FXML
    private void clearFilters() {
        txtSearch.clear();
        filteredCustomers.setPredicate(customer -> true);
        updateCounter();
    }

    // ========== DETAIL ==========
    private void showDetail(Customer c) {
        // Nombre completo (siempre debería tener, pero por si acaso)
        lblDetailFullName.setText(getDisplayText(c.getName() + " " + c.getSurname()));

        // Estado
        lblDetailStatus.setText(getDisplayText(c.getStatusName()));
        // Color según estado
        if ("Activo".equals(c.getStatusName())) {
            lblDetailStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else if ("Inactivo".equals(c.getStatusName())) {
            lblDetailStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            lblDetailStatus.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        }

        lblDetailDocumentType.setText(getDisplayText(c.getDocumentTypeName()));
        lblDetailDocumentNumber.setText(getDisplayText(c.getDocumentNumber()));
        lblDetailPhone.setText(getDisplayText(c.getPhoneNumber()));
        lblDetailEmail.setText(getDisplayText(c.getEmail()));
        lblDetailCountry.setText(getDisplayText(c.getCountryName()));
        lblDetailOrigin.setText(getDisplayText(c.getOriginName()));
    }

    private void clearDetail() {
        lblDetailFullName.setText("Seleccione un cliente");
        lblDetailStatus.setText("");
        lblDetailDocumentType.setText("--");
        lblDetailDocumentNumber.setText("--");
        lblDetailPhone.setText("--");
        lblDetailEmail.setText("--");
        lblDetailCountry.setText("--");
        lblDetailOrigin.setText("--");
    }

    private String getDisplayText(String value) {
        return (value != null && !value.isEmpty()) ? value : "--";
    }

    // ========== CRUD ==========
    private void openCustomerForm(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CustomerFormView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableCustomers.getScene().getWindow());

            CustomerFormController controller = loader.getController();
            if (customer != null) {
                controller.setCustomer(customer);
                stage.setTitle("Modificación de Cliente");
            } else {
                stage.setTitle("Registro de Cliente");
            }

            stage.showAndWait();
            loadActiveCustomers();
            tableCustomers.refresh();
            updateCounter();
        } catch (IOException e) {
            showAlert("Error", "No se pudo abrir el formulario", e.getMessage());
        }
    }

    private void openInactiveCustomersWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/InactiveCustomersView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Vista de Inactivos");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableCustomers.getScene().getWindow());
            stage.showAndWait();
            loadActiveCustomers();
            tableCustomers.refresh();
            updateCounter();
        } catch (IOException e) {
            showAlert("Error", "No se pudo abrir la ventana de inactivos", e.getMessage());
        }
    }

    private void deactivateCustomer() {
        Customer selected = tableCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar cliente");
        alert.setHeaderText("¿Desea eliminar este cliente?");
        alert.setContentText("El cliente " + selected.getName() + " " + selected.getSurname() + " no podrá realizar reservas.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    selected.setIdCustomerStatus(getStatusIdByName("inactive"));
                    if (customerDAO.isUpdate(selected)) {
                        masterCustomerList.remove(selected);
                        filteredCustomers.remove(selected);
                        tableCustomers.refresh();
                        clearDetail();
                        updateCounter();
                        showAlert("Éxito", "Cliente eliminado", "");
                    }
                } catch (SQLException e) {
                    showAlert("Error", "No se pudo eliminar", e.getMessage());
                }
            }
        });
    }

    // ========== HELPERS ==========
    private int getStatusIdByName(String name) {
        try {
            Map<Integer, String> statuses = new CustomerStatusDAO().listAll();
            for (Map.Entry<Integer, String> entry : statuses.entrySet()) {
                if (entry.getValue().equals(name)) return entry.getKey();
            }
        } catch (SQLException e) { /* ignore */ }
        return 1;
    }

    private void updateCounter() {
        lblTotalCustomers.setText("Mostrando " + (filteredCustomers != null ? filteredCustomers.size() : 0) + " clientes");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}