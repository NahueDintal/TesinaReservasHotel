package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Service;
import repositories.ServiceRepo;
import utils.StyleManager;

import java.math.BigDecimal;

public class InactiveServicesController {

    @FXML
    private TableView<Service> tableInactiveServices;

    @FXML
    private TableColumn<Service, String> colName;

    @FXML
    private TableColumn<Service, String> colDescription;

    @FXML
    private TableColumn<Service, BigDecimal> colPrice;

    @FXML
    private Label lblTotalServices;

    @FXML
    private Button btnActivate;

    @FXML
    private Button btnClose;

    private final ServiceRepo serviceRepo =
            new ServiceRepo();

    private final ObservableList<Service> inactiveServices =
            FXCollections.observableArrayList();

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

        loadInactiveServices();

        tableInactiveServices
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            btnActivate.setDisable(
                                    newValue == null
                            );
                        }
                );

        btnActivate.setOnAction(
                e -> activateService()
        );

        btnClose.setOnAction(
                e -> closeWindow()
        );
    }

    private void loadInactiveServices() {

        inactiveServices.setAll(
                serviceRepo.getInactiveServices()
        );

        tableInactiveServices.setItems(
                inactiveServices
        );

        updateCounter();
    }

    private void activateService() {

        Service selected =
                tableInactiveServices
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {
            return;
        }

        boolean confirmed =
                StyleManager.showConfirmation(
                        "Reactivar servicio",
                        "¿Desea reactivar este servicio?",
                        "El servicio "
                                + selected.getName()
                                + " volverá a estar disponible para nuevos consumos."
                );

        if (!confirmed) {
            return;
        }

        boolean success =
                serviceRepo.activate(
                        selected.getIdService()
                );

        if (success) {

            inactiveServices.remove(
                    selected
            );

            tableInactiveServices.refresh();

            btnActivate.setDisable(true);

            updateCounter();

            showAlert(
                    "Éxito",
                    "Servicio reactivado",
                    "El servicio se reactivó correctamente."
            );

        } else {

            showAlert(
                    "Error",
                    "No se pudo reactivar",
                    "No fue posible reactivar el servicio."
            );
        }
    }

    private void updateCounter() {

        lblTotalServices.setText(
                "Mostrando "
                        + inactiveServices.size()
                        + " servicios"
        );
    }

    private void closeWindow() {

        Stage stage =
                (Stage) btnClose
                        .getScene()
                        .getWindow();

        stage.close();
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

