package controllers;

import models.Staff;
import models.StaffStatus;
import repositories.StaffDAO;
import utils.Alerts;
import utils.StyleManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import java.time.format.DateTimeFormatter;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.SQLException;

public class StaffController {

  // ---------- Tabla principal ----------
  @FXML
  private TableView<Staff> tableStaff;
  @FXML
  private TableColumn<Staff, String> colFullName;
  @FXML
  private TableColumn<Staff, String> colPosition;
  @FXML
  private TableColumn<Staff, String> colPhone;
  // colId, colDni y colStatus ya no son columnas visibles en esta pantalla,
  // pero los datos siguen existiendo en el modelo Staff (id, dni, status)
  // y se ven en el panel de detalle y en el formulario.

  // ---------- Buscador y filtro ----------
  @FXML
  private TextField txtSearch;

  // ---------- Panel de detalle ----------
  @FXML
  private Label lblDetailName;
  @FXML
  private Label lblDetailPosition;
  @FXML
  private Label lblDetailStatus;
  @FXML
  private Label lblDetailDni;
  @FXML
  private Label lblDetailBirthDate;
  @FXML
  private Label lblDetailPhone;
  @FXML
  private Label lblDetailEmail;
  @FXML
  private Label lblDetailAddress;
  @FXML
  private Label lblDetailHireDate;
  @FXML
  private Label lblDetailShift;
  @FXML
  private Label lblDetailSalary;

  private final StaffDAO staffDAO = new StaffDAO();
  private ObservableList<Staff> staffList;
  private FilteredList<Staff> filteredStaffList;

  @FXML
  public void initialize() {
    configurarColumnas();
    cargarDatos();
    configurarBuscador();
    configurarSeleccionDeFila();
  }

  private void configurarColumnas() {
    colFullName
        .setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
    colPosition.setCellValueFactory(new PropertyValueFactory<>("positionName"));
    colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
  }

  // Trae todos los empleados desde la base de datos y los carga en la tabla
  private void cargarDatos() {
    try {
      staffList = FXCollections.observableArrayList(staffDAO.listAll());
      filteredStaffList = new FilteredList<>(staffList, s -> true);
      tableStaff.setItems(filteredStaffList);
      aplicarFiltros(); // arranca mostrando solo Activos desde el primer momento
    } catch (SQLException e) {
      mostrarAlerta("No se pudo cargar la lista de personal: " + e.getMessage());
    }
  }

  private void configurarBuscador() {
    txtSearch.textProperty().addListener((obs, oldValue, newValue) -> aplicarFiltros());
  }

  private void aplicarFiltros() {
    String texto = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();

    filteredStaffList.setPredicate(staff -> {
      // La tabla principal solo muestra Activos; los inactivos solo se ven
      // en la ventana "Eliminados" (antes "Inactivos").
      boolean esActivo = staff.getStatus() == StaffStatus.ACTIVE;

      boolean coincideTexto = texto.isEmpty()
          || staff.getFullName().toLowerCase().contains(texto)
          || staff.getDni().toLowerCase().contains(texto)
          || (staff.getPositionName() != null && staff.getPositionName().toLowerCase().contains(texto))
          || (staff.getPhone() != null && staff.getPhone().toLowerCase().contains(texto))
          || (staff.getEmail() != null && staff.getEmail().toLowerCase().contains(texto))
          || (staff.getCity() != null && staff.getCity().toLowerCase().contains(texto));

      return esActivo && coincideTexto;
    });
  }

  private void configurarSeleccionDeFila() {
    tableStaff.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldSelection, newSelection) -> mostrarDetalle(newSelection));
  }

  private void mostrarDetalle(Staff staff) {
    if (staff == null)
      return;

    lblDetailName.setText(staff.getFullName());
    lblDetailPosition.setText(staff.getPositionName());
    lblDetailStatus.setText(staff.getStatus().toString());
    lblDetailDni.setText(staff.getDni());
    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    lblDetailBirthDate.setText(staff.getBirthDate() != null ? staff.getBirthDate().format(df) : "-");
    lblDetailPhone.setText(staff.getPhone());
    lblDetailEmail.setText(staff.getEmail());
    lblDetailAddress.setText(staff.getStreet() + " " + staff.getAddressNumber() + ", " + staff.getCity());
    lblDetailHireDate.setText(staff.getHireDate() != null ? staff.getHireDate().format(df) : "-");
    String turnoTexto = staff.getShiftName() != null ? staff.getShiftName() : "Sin turno asignado";
    if (staff.getShiftStart() != null && staff.getShiftEnd() != null) {
      turnoTexto += " (" + staff.getShiftStart() + " - " + staff.getShiftEnd() + ")";
    }
    lblDetailShift.setText(turnoTexto);
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

      StyleManager.applyStyles(stage);
      stage.initOwner(tableStaff.getScene().getWindow());
      stage.initModality(Modality.APPLICATION_MODAL);

      // 🎯 Mientras el form esté abierto, los Alerts se centran sobre el form
      javafx.stage.Window previousOwner = StyleManager.getOwner();
      stage.setOnShown(e -> StyleManager.setOwner(stage));
      stage.setOnHidden(e -> {
        StyleManager.setOwner(previousOwner);
        cargarDatos();
        aplicarFiltros();
      });

      stage.showAndWait();

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

    boolean confirmado = Alerts.confirm(
        "Inactivar personal",
        "¿Seguro que querés inactivar a " + seleccionado.getFullName() + "?",
        "El empleado pasará al listado de inactivos y podrá reactivarse después.");

    if (!confirmado)
      return;

    try {
      staffDAO.deactivate(seleccionado.getId());
      cargarDatos();
      aplicarFiltros();
      // mostrarAlerta("El registro del personal fue inactivado satisfactoriamente.");
    } catch (SQLException e) {
      mostrarAlerta("No se pudo inactivar: " + e.getMessage());
    }
  }

  @FXML
  private void handleViewHistory() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/StaffHistory.fxml"));
      Parent root = loader.load();

      Stage stage = new Stage();
      stage.setTitle("Personal Inactivo");
      stage.setScene(new Scene(root));

      // 🎨 Mismo CSS que el resto
      StyleManager.applyStyles(stage);

      // 🎯 Modal + centrada sobre la ventana principal
      stage.initOwner(tableStaff.getScene().getWindow());
      stage.initModality(Modality.WINDOW_MODAL);

      // Al cerrar, refrescamos la tabla principal
      stage.setOnHidden(e -> {
        cargarDatos();
        aplicarFiltros();
      });

      stage.show();

    } catch (IOException e) {
      mostrarAlerta("No se pudo abrir el listado de inactivos: " + e.getMessage());
    }
  }

  private void mostrarAlerta(String mensaje) {
    // Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
    // alert.showAndWait();
    Alerts.info("Personal", mensaje);
  }
}
