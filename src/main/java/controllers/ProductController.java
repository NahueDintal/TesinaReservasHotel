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
import models.Product;
import repositories.ProductRepo;
import utils.StyleManager;

import java.io.IOException;

public class ProductController {

    // ========== TABLE ==========
    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, Object> colPrice;

    // ========== BUTTONS ==========
    @FXML private Button btnNewProduct;
    @FXML private Button btnViewInactive;
    @FXML private Button btnEdit;
    @FXML private Button btnDeactivate;

    // ========== DETAIL ==========
    @FXML private TextField txtDetailName;
    @FXML private TextField txtDetailDescription;
    @FXML private TextField txtDetailPrice;

    // ========== SEARCH ==========
    @FXML private TextField txtSearch;
    @FXML private Label lblTotalProducts;

    // ========== REPOSITORY ==========
    private final ProductRepo productRepo = new ProductRepo();

    private final ObservableList<Product> masterProductList =
            FXCollections.observableArrayList();

    private FilteredList<Product> filteredProducts;

    // ========== INIT ==========
    @FXML
    public void initialize() {

        // Configurar columnas
        colName.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        colDescription.setCellValueFactory(
                new PropertyValueFactory<>("description")
        );

        colPrice.setCellValueFactory(
                new PropertyValueFactory<>("price")
        );

        // Cargar productos activos
        loadActiveProducts();

        // Configurar buscador
        txtSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue == null || newValue.trim().isEmpty()) {
                        loadActiveProducts();
                    } else {
                        masterProductList.setAll(
                                productRepo.searchProducts(newValue.trim())
                        );

                        filteredProducts =
                                new FilteredList<>(masterProductList, p -> true);

                        tableProducts.setItems(filteredProducts);

                        updateCounter();
                    }
                }
        );

        // Selección de producto
        tableProducts.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    if (newValue != null) {
                        showDetail(newValue);
                    } else {
                        clearDetail();
                    }
                });

        // Desactivar botones hasta seleccionar
        btnEdit.setDisable(true);
        btnDeactivate.setDisable(true);

        tableProducts.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    boolean selected = newValue != null;

                    btnEdit.setDisable(!selected);
                    btnDeactivate.setDisable(!selected);
                });

        // Acciones
        btnNewProduct.setOnAction(e -> openProductForm(null));

        btnViewInactive.setOnAction(
                e -> openInactiveProductsWindow()
        );

        btnEdit.setOnAction(
                e -> openProductForm(
                        tableProducts.getSelectionModel().getSelectedItem()
                )
        );

        btnDeactivate.setOnAction(
                e -> deactivateProduct()
        );
    }

    // ========== LOAD ==========

    private void loadActiveProducts() {

        masterProductList.setAll(
                productRepo.getActiveProducts()
        );

        filteredProducts =
                new FilteredList<>(masterProductList, p -> true);

        tableProducts.setItems(filteredProducts);

        updateCounter();
    }

    // ========== DETAIL ==========

    private void showDetail(Product product) {

        txtDetailName.setText(
                getDisplayText(product.getName())
        );

        txtDetailDescription.setText(
                getDisplayText(product.getDescription())
        );

        txtDetailPrice.setText(
                product.getPrice() != null
                        ? product.getPrice().toString()
                        : "--"
        );
    }

    private void clearDetail() {

        txtDetailName.setText("Seleccione un producto");
        txtDetailDescription.setText("--");
        txtDetailPrice.setText("--");
    }

    private String getDisplayText(String value) {

        return value != null && !value.isEmpty()
                ? value
                : "--";
    }

    // ========== CRUD ==========

    private void openProductForm(Product product) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/ProductFormView.fxml")
            );

            Stage stage = new Stage();

            stage.setScene(
                    new Scene(loader.load())
            );

            StyleManager.applyStyles(stage);

            stage.initModality(Modality.WINDOW_MODAL);

            stage.initOwner(
                    tableProducts.getScene().getWindow()
            );

            ProductFormController controller =
                    loader.getController();

            if (product != null) {

                controller.setProduct(product);

                stage.setTitle("Modificación de Producto");

            } else {

                stage.setTitle("Registro de Producto");
            }

            stage.showAndWait();

            loadActiveProducts();

            tableProducts.refresh();

            updateCounter();

        } catch (IOException e) {

            showAlert(
                    "Error",
                    "No se pudo abrir el formulario",
                    e.getMessage()
            );
        }
    }

    private void openInactiveProductsWindow() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/views/InactiveProductsView.fxml"
                    )
            );

            Stage stage = new Stage();

            stage.setScene(
                    new Scene(loader.load())
            );

            StyleManager.applyStyles(stage);

            stage.setTitle("Productos Eliminados");

            stage.initModality(Modality.WINDOW_MODAL);

            stage.initOwner(
                    tableProducts.getScene().getWindow()
            );

            stage.showAndWait();

            loadActiveProducts();

            tableProducts.refresh();

            updateCounter();

        } catch (IOException e) {

            showAlert(
                    "Error",
                    "No se pudo abrir la ventana de inactivos",
                    e.getMessage()
            );
        }
    }

    private void deactivateProduct() {

        Product selected =
                tableProducts.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        boolean confirmed =
                StyleManager.showConfirmation(
                        "Eliminar producto",
                        "¿Desea eliminar este producto?",
                        "El producto "
                                + selected.getName()
                                + " dejará de estar disponible para nuevos consumos."
                );

        if (confirmed) {

            boolean success =
                    productRepo.deactivate(
                            selected.getIdProduct()
                    );

            if (success) {

                masterProductList.remove(selected);

                if (filteredProducts != null) {
                    filteredProducts.remove(selected);
                }

                tableProducts.refresh();

                clearDetail();

                btnEdit.setDisable(true);
                btnDeactivate.setDisable(true);

                updateCounter();

                showAlert(
                        "Éxito",
                        "Producto eliminado",
                        "El producto se desactivó correctamente."
                );

            } else {

                showAlert(
                        "Error",
                        "No se pudo eliminar",
                        "No fue posible desactivar el producto."
                );
            }
        }
    }

    // ========== COUNTER ==========

    private void updateCounter() {

        int count =
                filteredProducts != null
                        ? filteredProducts.size()
                        : 0;

        lblTotalProducts.setText(
                "Mostrando " + count + " productos"
        );
    }

    // ========== ALERT ==========

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

