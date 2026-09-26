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
import utils.StyleManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CustomerController {

    // ========== TABLE ==========
    @FXML private TableView<Customer> tableCustomers;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colSurname;
    @FXML private TableColumn<Customer, String> colDocumentType;
    @FXML private TableColumn<Customer, String> colOrigin;
    @FXML private TableColumn<Customer, String> colCountry;

    // ========== BUTTONS ==========
    @FXML private Button btnNewCustomer;
    @FXML private Button btnViewInactive;
    @FXML private Button btnEdit;
    @FXML private Button btnDeactivate;
    @FXML private Button btnHistory;


    // ========== DETAIL ==========
    @FXML private TextField txtDetailFullName;
    @FXML private TextField txtDetailDocumentType;
    @FXML private TextField txtDetailDocumentNumber;
    @FXML private TextField txtDetailPhone;
    @FXML private TextField txtDetailEmail;
    @FXML private TextField txtDetailCountry;
    @FXML private TextField txtDetailOrigin;

    // ========== SEARCH ==========
    @FXML private TextField txtSearch;
    @FXML private Label lblTotalCustomers;

    // ========== DAOs ==========
    private CustomerDAO customerDAO = new CustomerDAO();
    private ObservableList<Customer> masterCustomerList = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredCustomers;

    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    // ========== INIT ==========
    @FXML
    public void initialize() {
        // Configurar columnas
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colDocumentType.setCellValueFactory(new PropertyValueFactory<>("documentTypeName"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("originName"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("countryName"));

        // Cargar clientes activos
        loadActiveCustomers();

        // Configurar buscador
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.trim().isEmpty()) {
                    loadActiveCustomers();
                } else {
                    // Si hay texto, busca 100 recientes
                    masterCustomerList.setAll(customerDAO.searchCustomers(newValue.trim()));
                    filteredCustomers = new FilteredList<>(masterCustomerList, p -> true);
                    tableCustomers.setItems(filteredCustomers);
                    tableCustomers.refresh();
                    updateCounter();
                }
            } catch (SQLException e) {
                showAlert("Error", "No se pudo realizar la búsqueda", e.getMessage());
            }
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
        btnHistory.setDisable(true);

        tableCustomers.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    boolean selected = newVal != null;
                    btnEdit.setDisable(!selected);
                    btnDeactivate.setDisable(!selected);
                    btnHistory.setDisable(!selected);
                }
        );

        // Acciones
        btnNewCustomer.setOnAction(e -> openCustomerForm(null));
        btnViewInactive.setOnAction(e -> openInactiveCustomersWindow());
        btnEdit.setOnAction(e -> openCustomerForm(tableCustomers.getSelectionModel().getSelectedItem()));
        btnDeactivate.setOnAction(e -> deactivateCustomer());
        btnHistory.setOnAction(e -> openCustomerHistory());
    }

    // ========== LOAD ==========
    private void loadActiveCustomers() {
        try {
            masterCustomerList.setAll(customerDAO.listAll());
            filteredCustomers = new FilteredList<>(masterCustomerList, p -> true);
            tableCustomers.setItems(filteredCustomers);
            //tableCustomers.refresh();
            updateCounter();
        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los clientes", e.getMessage());
        }
    }

    // ========== DETAIL ==========
    private void showDetail(Customer c) {
        txtDetailFullName.setText(getDisplayText(c.getName() + " " + c.getSurname()));
        txtDetailDocumentType.setText(getDisplayText(c.getDocumentTypeName()));
        txtDetailDocumentNumber.setText(getDisplayText(c.getDocumentNumber()));
        txtDetailPhone.setText(getDisplayText(c.getPhoneNumber()));
        txtDetailEmail.setText(getDisplayText(c.getEmail()));
        txtDetailCountry.setText(getDisplayText(c.getCountryName()));
        txtDetailOrigin.setText(getDisplayText(c.getOriginName()));
    }

    private void clearDetail() {
        txtDetailFullName.setText("Seleccione un cliente");
        txtDetailDocumentType.setText("--");
        txtDetailDocumentNumber.setText("--");
        txtDetailPhone.setText("--");
        txtDetailEmail.setText("--");
        txtDetailCountry.setText("--");
        txtDetailOrigin.setText("--");
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
            StyleManager.applyStyles(stage);
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
            StyleManager.applyStyles(stage);
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

    private void openCustomerHistory() {
        Customer selected = tableCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CustomerHistoryView.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());

            StyleManager.applyStyles(scene);

            stage.setScene(scene);
            stage.setTitle("Historial de reservas");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tableCustomers.getScene().getWindow());

            CustomerHistoryController controller = loader.getController();
            controller.setCustomer(selected);
            controller.setDashboardController(dashboardController); // ← NUEVO

            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "No se pudo abrir el historial", e.getMessage());
        }
    }

    private void deactivateCustomer() {
        Customer selected = tableCustomers.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean confirmed = StyleManager.showConfirmation("Eliminar cliente",
                "¿Desea eliminar este cliente?",
                "El cliente " + selected.getName() + " " + selected.getSurname() + " no podrá realizar reservas.");

        if (confirmed) {
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
        int count = filteredCustomers != null ? filteredCustomers.size() : 0;

        // Verificar si hay más de 100 clientes en total (haciendo una consulta rápida)
        try {
            int totalInDB = customerDAO.countAll();
            if (totalInDB > 100) {
                lblTotalCustomers.setText("Mostrando los últimos 100 de " + totalInDB + " clientes");
            } else {
                lblTotalCustomers.setText("Mostrando " + count + " clientes");
            }
        } catch (SQLException e) {
            lblTotalCustomers.setText("Mostrando " + count + " clientes");
        }
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