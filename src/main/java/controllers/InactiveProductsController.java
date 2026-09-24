package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Product;
import repositories.ProductRepo;
import utils.StyleManager;

import java.math.BigDecimal;

public class InactiveProductsController {

    @FXML
    private TableView<Product> tableInactiveProducts;

    @FXML
    private TableColumn<Product, String> colName;

    @FXML
    private TableColumn<Product, String> colDescription;

    @FXML
    private TableColumn<Product, BigDecimal> colPrice;

    @FXML
    private Label lblTotalProducts;

    @FXML
    private Button btnActivate;

    @FXML
    private Button btnClose;

    private final ProductRepo productRepo = new ProductRepo();

    private final ObservableList<Product> inactiveProducts =
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

        loadInactiveProducts();

        tableInactiveProducts.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    btnActivate.setDisable(newValue == null);

                });

        btnActivate.setOnAction(e -> activateProduct());

        btnClose.setOnAction(e -> closeWindow());
    }

    private void loadInactiveProducts() {

        inactiveProducts.setAll(
                productRepo.getInactiveProducts()
        );

        tableInactiveProducts.setItems(inactiveProducts);

        updateCounter();
    }

    private void activateProduct() {

        Product selected =
                tableInactiveProducts
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {
            return;
        }

        boolean confirmed =
                StyleManager.showConfirmation(
                        "Reactivar producto",
                        "¿Desea reactivar este producto?",
                        "El producto "
                                + selected.getName()
                                + " volverá a estar disponible para nuevos consumos."
                );

        if (!confirmed) {
            return;
        }

        boolean success =
                productRepo.activate(
                        selected.getIdProduct()
                );

        if (success) {

            inactiveProducts.remove(selected);

            tableInactiveProducts.refresh();

            btnActivate.setDisable(true);

            updateCounter();

            showAlert(
                    "Éxito",
                    "Producto reactivado",
                    "El producto se reactivó correctamente."
            );

        } else {

            showAlert(
                    "Error",
                    "No se pudo reactivar",
                    "No fue posible reactivar el producto."
            );
        }
    }

    private void updateCounter() {

        lblTotalProducts.setText(
                "Mostrando "
                        + inactiveProducts.size()
                        + " productos"
        );
    }

    private void closeWindow() {

        Stage stage =
                (Stage) btnClose.getScene().getWindow();

        stage.close();
    }

    private void showAlert(
            String title,
            String header,
            String content
    ) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        StyleManager.applyStyles(
                alert.getDialogPane()
        );

        alert.showAndWait();
    }
}

