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

    private final ObservableList<Service> masterServiceList =
            FXCollections.observableArrayList();

    private FilteredList<Service> filteredServices;

    @FXML
    public void initialize() {

        colName.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        colDescription.setCellValueFactory(
                new PropertyValueFactory<>("description")
        );

        colPrice.setCellValueFactory(
                new PropertyValueFactory<>("price")
        );

        loadActiveServices();

        txtSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue == null || newValue.trim().isEmpty()) {

                        loadActiveServices();

                    } else {

                        masterServiceList.setAll(
                                serviceRepo.searchServices(
                                        newValue.trim()
                                )
                        );

                        filteredServices =
                                new FilteredList<>(
                                        masterServiceList,
                                        service -> true
                                );

                        tableServices.setItems(filteredServices);

                        updateCounter();
                    }
                }
        );

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
                e -> openServiceForm(null)
        );

        btnViewInactive.setOnAction(
                e -> openInactiveServicesWindow()
        );

        btnEdit.setOnAction(
                e -> openServiceForm(
                        tableServices
                                .getSelectionModel()
                                .getSelectedItem()
                )
        );

        btnDeactivate.setOnAction(
                e -> deactivateService()
        );
    }

    private void loadActiveServices() {

        masterServiceList.setAll(
                serviceRepo.getActiveServices()
        );

        filteredServices =
                new FilteredList<>(
                        masterServiceList,
                        service -> true
                );

        tableServices.setItems(filteredServices);

        updateCounter();
    }

    private void showDetail(Service service) {

        txtDetailName.setText(
                getDisplayText(service.getName())
        );

        txtDetailDescription.setText(
                getDisplayText(service.getDescription())
        );

        txtDetailPrice.setText(
                service.getPrice() != null
                        ? service.getPrice().toString()
                        : "--"
        );
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

    private void openServiceForm(Service service) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/views/ServiceFormView.fxml"
                            )
                    );

            Stage stage = new Stage();

            stage.setScene(
                    new Scene(loader.load())
            );

            StyleManager.applyStyles(stage);

            stage.initModality(Modality.WINDOW_MODAL);

            stage.initOwner(
                    tableServices.getScene().getWindow()
            );

            ServiceFormController controller =
                    loader.getController();

            if (service != null) {

                controller.setService(service);

                stage.setTitle(
                        "Modificación de Servicio"
                );

            } else {

                stage.setTitle(
                        "Registro de Servicio"
                );
            }

            stage.showAndWait();

            loadActiveServices();

            tableServices.refresh();

            updateCounter();

        } catch (IOException e) {

            showAlert(
                    "Error",
                    "No se pudo abrir el formulario",
                    e.getMessage()
            );
        }
    }

    private void openInactiveServicesWindow() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/views/InactiveServiceView.fxml"
                            )
                    );

            Stage stage = new Stage();

            stage.setScene(
                    new Scene(loader.load())
            );

            StyleManager.applyStyles(stage);

            stage.setTitle("Servicios Eliminados");

            stage.initModality(Modality.WINDOW_MODAL);

            stage.showAndWait();

            loadActiveServices();
            tableServices.refresh();
            updateCounter();

        } catch (Exception e) {
            e.printStackTrace();

            showAlert(
                    "Error",
                    "No se pudo abrir la ventana de inactivos",
                    e.getMessage()
            );
        }
    }


    private void deactivateService() {

        Service selected =
                tableServices
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {
            return;
        }

        boolean confirmed =
                StyleManager.showConfirmation(
                        "Eliminar servicio",
                        "¿Desea eliminar este servicio?",
                        "El servicio "
                                + selected.getName()
                                + " dejará de estar disponible para nuevos consumos."
                );

        if (confirmed) {

            boolean success =
                    serviceRepo.deactivate(
                            selected.getIdService()
                    );

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
                        "El servicio se desactivó correctamente."
                );

            } else {

                showAlert(
                        "Error",
                        "No se pudo eliminar",
                        "No fue posible desactivar el servicio."
                );
            }
        }
    }

    private void updateCounter() {

        int count =
                filteredServices != null
                        ? filteredServices.size()
                        : 0;

        lblTotalServices.setText(
                "Mostrando "
                        + count
                        + " servicios"
        );
    }

    private void showAlert(
            String title,
            String header,
            String content
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        StyleManager.applyStyles(
                alert.getDialogPane()
        );

        alert.showAndWait();
    }
}

