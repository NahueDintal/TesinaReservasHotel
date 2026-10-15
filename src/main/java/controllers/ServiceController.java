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
import models.Service;
import repositories.ServiceRepo;
import utils.StyleManager;

import java.io.IOException;
import java.math.BigDecimal;

public class ServiceController {

  @FXML
  private TableView<Service> tableServices;

  @FXML
  private TableColumn<Service, String> colName;

  @FXML
  private TableColumn<Service, String> colDescription;

  @FXML
  private TableColumn<Service, BigDecimal> colPrice;

  @FXML
  private Button btnNewService;

  @FXML
  private Button btnViewInactive;

  @FXML
  private Button btnEdit;

  @FXML
  private Button btnDeactivate;

  @FXML
  private TextField txtDetailName;

  @FXML
  private TextField txtDetailDescription;

  @FXML
  private TextField txtDetailPrice;

  @FXML
  private TextField txtSearch;

  @FXML
  private Label lblTotalServices;

  private final ServiceRepo serviceRepo = new ServiceRepo();

  private final ObservableList<Service> masterServiceList = FXCollections.observableArrayList();

  private FilteredList<Service> filteredServices;

  // 🔑 Referencias a ventanas hijas (para evitar abrir múltiples veces)
  private Stage serviceFormStage;
  private Stage inactiveServicesStage;

  @FXML
  public void initialize() {

    colName.setCellValueFactory(
        new PropertyValueFactory<>("name"));

    colDescription.setCellValueFactory(
        new PropertyValueFactory<>("description"));

    colPrice.setCellValueFactory(
        new PropertyValueFactory<>("price"));

    loadActiveServices();

    txtSearch.textProperty().addListener(
        (observable, oldValue, newValue) -> {

          if (newValue == null || newValue.trim().isEmpty()) {

            loadActiveServices();

          } else {

            masterServiceList.setAll(
                serviceRepo.searchServices(
                    newValue.trim()));

            filteredServices = new FilteredList<>(
                masterServiceList,
                service -> true);

            tableServices.setItems(filteredServices);

            updateCounter();
          }
        });

    tableServices.getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, oldValue, newValue) -> {

          if (newValue != null) {
            showDetail(newValue);
          } else {
            clearDetail();
          }

          boolean selected = newValue != null;

          btnEdit.setDisable(!selected);
          btnDeactivate.setDisable(!selected);
        });

    btnEdit.setDisable(true);
    btnDeactivate.setDisable(true);

    btnNewService.setOnAction(
        e -> openServiceForm(null));

    btnViewInactive.setOnAction(
        e -> openInactiveServicesWindow());

    btnEdit.setOnAction(
        e -> openServiceForm(
            tableServices
                .getSelectionModel()
                .getSelectedItem()));

    btnDeactivate.setOnAction(
        e -> deactivateService());
  }

  private void loadActiveServices() {

    masterServiceList.setAll(
        serviceRepo.getActiveServices());

    filteredServices = new FilteredList<>(
        masterServiceList,
        service -> true);

    tableServices.setItems(filteredServices);

    updateCounter();
  }

  private void showDetail(Service service) {

    txtDetailName.setText(
        getDisplayText(service.getName()));

    txtDetailDescription.setText(
        getDisplayText(service.getDescription()));

    txtDetailPrice.setText(
        service.getPrice() != null
            ? service.getPrice().toString()
            : "--");
  }

  private void clearDetail() {

    txtDetailName.setText("Seleccione un servicio");
    txtDetailDescription.setText("--");
    txtDetailPrice.setText("--");
  }

  private String getDisplayText(String value) {

    return value != null && !value.isEmpty()
        ? value
        : "--";
  }

  // ============================================================
  // FORM DE SERVICIO (Nuevo / Editar)
  // ============================================================

  private void openServiceForm(Service service) {

    // 🔑 Si ya hay un form abierto, traerlo al frente y salir
    if (serviceFormStage != null && serviceFormStage.isShowing()) {
      serviceFormStage.toFront();
      serviceFormStage.requestFocus();
      return;
    }

    try {

      FXMLLoader loader = new FXMLLoader(
          getClass().getResource(
              "/views/ServiceFormView.fxml"));

      serviceFormStage = new Stage();

      serviceFormStage.setScene(
          new Scene(loader.load()));

      StyleManager.applyStyles(serviceFormStage);

      serviceFormStage.initModality(Modality.WINDOW_MODAL);

      serviceFormStage.initOwner(
          tableServices.getScene().getWindow());

      ServiceFormController controller = loader.getController();

      if (service != null) {

        controller.setService(service);

        serviceFormStage.setTitle(
            "Modificación de Servicio");

      } else {

        serviceFormStage.setTitle(
            "Registro de Servicio");
      }

      // 🔑 Al cerrarse: limpiar referencia + recargar
      serviceFormStage.setOnHidden(evt -> {
        serviceFormStage = null;
        loadActiveServices();
        tableServices.refresh();
        updateCounter();
      });

      serviceFormStage.showAndWait();

    } catch (IOException e) {

      serviceFormStage = null;

      showAlert(
          "Error",
          "No se pudo abrir el formulario",
          e.getMessage());
    }
  }

  // ============================================================
  // VENTANA DE SERVICIOS ELIMINADOS
  // ============================================================

  private void openInactiveServicesWindow() {

    // 🔑 Si ya está abierta, traerla al frente y salir
    if (inactiveServicesStage != null && inactiveServicesStage.isShowing()) {
      inactiveServicesStage.toFront();
      inactiveServicesStage.requestFocus();
      return;
    }

    try {

      FXMLLoader loader = new FXMLLoader(
          getClass().getResource(
              "/views/InactiveServiceView.fxml"));

      inactiveServicesStage = new Stage();

      inactiveServicesStage.setScene(
          new Scene(loader.load()));

      StyleManager.applyStyles(inactiveServicesStage);

      inactiveServicesStage.setTitle("Servicios Eliminados");

      inactiveServicesStage.initModality(Modality.WINDOW_MODAL);

      inactiveServicesStage.initOwner(
          tableServices.getScene().getWindow());

      // 🔑 Al cerrarse: limpiar referencia + recargar
      inactiveServicesStage.setOnHidden(evt -> {
        inactiveServicesStage = null;
        loadActiveServices();
        tableServices.refresh();
        updateCounter();
      });

      inactiveServicesStage.showAndWait();

    } catch (Exception e) {

      inactiveServicesStage = null;

      e.printStackTrace();

      showAlert(
          "Error",
          "No se pudo abrir la ventana de inactivos",
          e.getMessage());
    }
  }

  // ============================================================
  // ELIMINAR (soft delete)
  // ============================================================

  private void deactivateService() {

    Service selected = tableServices
        .getSelectionModel()
        .getSelectedItem();

    if (selected == null) {
      return;
    }

    boolean confirmed = StyleManager.showConfirmation(
        "Eliminar servicio",
        "¿Desea eliminar este servicio?",
        "El servicio "
            + selected.getName()
            + " dejará de estar disponible para nuevos consumos.");

    if (confirmed) {

      boolean success = serviceRepo.deactivate(
          selected.getIdService());

      if (success) {

        masterServiceList.remove(selected);

        tableServices.refresh();

        clearDetail();

        btnEdit.setDisable(true);
        btnDeactivate.setDisable(true);

        updateCounter();

        showAlert(
            "Éxito",
            "Servicio eliminado",
            "El servicio se desactivó correctamente.");

      } else {

        showAlert(
            "Error",
            "No se pudo eliminar",
            "No fue posible desactivar el servicio.");
      }
    }
  }

  private void updateCounter() {

    int count = filteredServices != null
        ? filteredServices.size()
        : 0;

    lblTotalServices.setText(
        "Mostrando "
            + count
            + " servicios");
  }

  private void showAlert(
      String title,
      String header,
      String content) {

    Alert alert = new Alert(
        Alert.AlertType.INFORMATION);

    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);

    StyleManager.applyStyles(
        alert.getDialogPane());

    alert.showAndWait();
  }
}
