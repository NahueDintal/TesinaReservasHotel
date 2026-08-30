package controllers;

import models.Staff;
import models.StaffStatus;
import repositories.StaffDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.SQLException;

public class StaffController {

    // ---------- Tabla principal ----------
    @FXML private TableView<Staff> tableStaff;
    @FXML private TableColumn<Staff, String> colId;
    @FXML private TableColumn<Staff, String> colFullName;
    @FXML private TableColumn<Staff, String> colDni;
    @FXML private TableColumn<Staff, String> colPosition;
    @FXML private TableColumn<Staff, String> colDepartment;
    @FXML private TableColumn<Staff, String> colPhone;
    @FXML private TableColumn<Staff, String> colStatus;

    // ---------- Buscador y filtro ----------
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboStatusFilter;

    // ---------- Panel de detalle ----------
    @FXML private Label lblDetailName;
    @FXML private Label lblDetailPosition;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailDni;
    @FXML private Label lblDetailBirthDate;
    @FXML private Label lblDetailPhone;
    @FXML private Label lblDetailEmail;
    @FXML private Label lblDetailAddress;
    @FXML private Label lblDetailDepartment;
    @FXML private Label lblDetailHireDate;
    @FXML private Label lblDetailShift;
    @FXML private Label lblDetailSalary;

    private final StaffDAO staffDAO = new StaffDAO();
    private ObservableList<Staff> staffList;
    private FilteredList<Staff> filteredStaffList;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarDatos();
        configurarFiltroDeEstado();
        configurarBuscador();
        configurarSeleccionDeFila();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFullName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("positionName"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));
    }

    // Trae todos los empleados desde la base de datos y los carga en la tabla
    private void cargarDatos() {
        try {
            staffList = FXCollections.observableArrayList(staffDAO.listAll());
            filteredStaffList = new FilteredList<>(staffList, s -> true);
            tableStaff.setItems(filteredStaffList);
        } catch (SQLException e) {
            mostrarAlerta("No se pudo cargar la lista de personal: " + e.getMessage());
        }
    }

    private void configurarFiltroDeEstado() {
        comboStatusFilter.setItems(FXCollections.observableArrayList("Todos", "Activo", "Inactivo"));
        comboStatusFilter.setValue("Activo");
        comboStatusFilter.setOnAction(e -> aplicarFiltros());
    }

    private void configurarBuscador() {
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String texto = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        String estadoSeleccionado = comboStatusFilter.getValue();

        filteredStaffList.setPredicate(staff -> {
            boolean coincideEstado = switch (estadoSeleccionado) {
                case "Activo" -> staff.getStatus() == StaffStatus.ACTIVE;
                case "Inactivo" -> staff.getStatus() == StaffStatus.INACTIVE;
                default -> true;
            };

            boolean coincideTexto = texto.isEmpty()
                    || staff.getFullName().toLowerCase().contains(texto)
                    || staff.getDni().toLowerCase().contains(texto)
                    || (staff.getPositionName() != null && staff.getPositionName().toLowerCase().contains(texto));

            return coincideEstado && coincideTexto;
        });
    }

    private void configurarSeleccionDeFila() {
        tableStaff.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> mostrarDetalle(newSelection)
        );
    }

    private void mostrarDetalle(Staff staff) {
        if (staff == null) return;

        lblDetailName.setText(staff.getFullName());
        lblDetailPosition.setText(staff.getPositionName());
        lblDetailStatus.setText(staff.getStatus().toString());
        lblDetailDni.setText(staff.getDni());
        lblDetailBirthDate.setText(staff.getBirthDate() != null ? staff.getBirthDate().toString() : "-");
        lblDetailPhone.setText(staff.getPhone());
        lblDetailEmail.setText(staff.getEmail());
        lblDetailAddress.setText(staff.getStreet() + " " + staff.getAddressNumber() + ", " + staff.getCity());
        lblDetailDepartment.setText(staff.getDepartmentName());
        lblDetailHireDate.setText(staff.getHireDate() != null ? staff.getHireDate().toString() : "-");
        lblDetailShift.setText(staff.getShiftName() != null
                ? staff.getShiftName() + " (" + staff.getShiftStart() + " - " + staff.getShiftEnd() + ")" : "-");
        lblDetailSalary.setText(staff.getSalary() != null ? "$ " + staff.getSalary() : "-");
    }

    @FXML
    private void handleNewStaff() {
        abrirFormulario(null);
    }

    @FXML
    private void handleEdit() {
        Staff seleccionado = tableStaff.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccioná un empleado de la tabla primero.");
            return;
        }
        abrirFormulario(seleccionado);
    }

    private void abrirFormulario(Staff staffAEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/StaffForm.fxml"));
            Parent root = loader.load();

            StaffFormController formController = loader.getController();
            if (staffAEditar != null) {
                formController.setStaff(staffAEditar);
            }

            Stage stage = new Stage();
            stage.setTitle(staffAEditar == null ? "Nuevo Personal" : "Editar Personal");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            cargarDatos();
            aplicarFiltros();

        } catch (IOException e) {
            mostrarAlerta("No se pudo abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeactivate() {
        Staff seleccionado = tableStaff.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Seleccioná un empleado de la tabla primero.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que querés inactivar a " + seleccionado.getFullName() + "?");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    staffDAO.deactivate(seleccionado.getId());
                    cargarDatos();
                    aplicarFiltros();
                } catch (SQLException e) {
                    mostrarAlerta("No se pudo inactivar: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleViewHistory() {
        mostrarAlerta("La funcionalidad de historial todavía no está implementada.");
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alert.showAndWait();
    }
}