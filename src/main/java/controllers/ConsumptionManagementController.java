package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Consumption;
import models.Product;
import models.Reservation;
import models.Service;
import repositories.ConsumptionRepo;
import repositories.ProductRepo;
import repositories.ServiceRepo;
import java.util.Optional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ConsumptionManagementController {

    @FXML
    private Label lblFormTitle;

    @FXML
    private Label lblReservation;

    @FXML
    private ComboBox<String> cmbConsumptionType;

    @FXML
    private ComboBox<Product> cmbProduct;

    @FXML
    private ComboBox<Service> cmbService;

    @FXML
    private TextField txtQuantity;

    @FXML
    private Button btnAdd;

    @FXML
    private TableView<Consumption> tblConsumptions;

    @FXML
    private TableColumn<Consumption, String> colType;

    @FXML
    private TableColumn<Consumption, String> colName;

    @FXML
    private TableColumn<Consumption, Integer> colQuantity;

    @FXML
    private TableColumn<Consumption, ?> colUnitPrice;

    @FXML
    private TableColumn<Consumption, ?> colTotal;

    @FXML
    private TableColumn<Consumption, Void> colActions;

    @FXML
    private TextField txtTotal;

    @FXML
    private Button btnCancel;

    private final ConsumptionRepo consumptionRepo;
    private final ProductRepo productRepo;
    private final ServiceRepo serviceRepo;

    private Reservation reservation;
    private Consumption consumptionBeingEdited;
    private DashboardController dashboardController;

    private final ObservableList<Consumption> consumptions =
            FXCollections.observableArrayList();

    public ConsumptionManagementController() {

        consumptionRepo = new ConsumptionRepo();
        productRepo = new ProductRepo();
        serviceRepo = new ServiceRepo();
    }

    @FXML
    public void initialize() {

        loadProducts();
        loadServices();

        cmbConsumptionType.getItems().addAll(
                "Producto",
                "Servicio"
        );

        cmbProduct.setDisable(true);
        cmbService.setDisable(true);

        txtTotal.setText("0.00");

        setupConsumptionType();

        configureConsumptionTable();

        btnCancel.setOnAction(event -> closeWindow());

        btnAdd.setOnAction(event -> handleAddConsumption());

        System.out.println(
                "ConsumptionManagementController inicializado."
        );
    }

    // =========================================================
    // CARGAR PRODUCTOS
    // =========================================================

    private void loadProducts() {

        try {

            List<Product> products =
                    productRepo.getActiveProducts();

            cmbProduct.getItems().setAll(products);

        } catch (Exception e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudieron cargar los productos."
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // CARGAR SERVICIOS
    // =========================================================

    private void loadServices() {

        try {

            List<Service> services =
                    serviceRepo.getActiveServices();

            cmbService.getItems().setAll(services);

        } catch (Exception e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudieron cargar los servicios."
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // CARGAR CONSUMOS DE LA RESERVA
    // =========================================================

    private void loadConsumptions() {

        if (reservation == null) {
            return;
        }

        try {

            List<Consumption> loaded =
                    consumptionRepo.getConsumptionsByReservation(
                            reservation.getIdReservation()
                    );

            consumptions.setAll(loaded);

            calculateTotal();

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudieron cargar los consumos de la reserva."
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // CONFIGURAR TABLA
    // =========================================================

    private void configureConsumptionTable() {

        // CANTIDAD
        colQuantity.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        // PRECIO UNITARIO
        colUnitPrice.setCellValueFactory(
                new PropertyValueFactory<>("unitPrice")
        );

        // TOTAL
        colTotal.setCellValueFactory(
                new PropertyValueFactory<>("total")
        );

        // TIPO
        colType.setCellValueFactory(cellData -> {

            Consumption consumption =
                    cellData.getValue();

            if (consumption.getIdConsumptionType() == 1) {
                return new javafx.beans.property.SimpleStringProperty(
                        "Producto"
                );
            }

            if (consumption.getIdConsumptionType() == 2) {
                return new javafx.beans.property.SimpleStringProperty(
                        "Servicio"
                );
            }

            return new javafx.beans.property.SimpleStringProperty(
                    ""
            );
        });

        // NOMBRE
        colName.setCellValueFactory(cellData -> {

            Consumption consumption =
                    cellData.getValue();

            if (consumption.getIdConsumptionType() == 1) {

                for (Product product : cmbProduct.getItems()) {

                    if (product.getIdProduct()
                            == consumption.getIdProduct()) {

                        return new javafx.beans.property.SimpleStringProperty(
                                product.getName()
                        );
                    }
                }

            } else if (consumption.getIdConsumptionType() == 2) {

                for (Service service : cmbService.getItems()) {

                    if (service.getIdService()
                            == consumption.getIdService()) {

                        return new javafx.beans.property.SimpleStringProperty(
                                service.getName()
                        );
                    }
                }
            }

            return new javafx.beans.property.SimpleStringProperty(
                    ""
            );
        });

        tblConsumptions.setItems(consumptions);

        colActions.setCellFactory(column ->
                new TableCell<Consumption, Void>() {

                    private final Button btnEdit =
                            new Button("Editar");

                    private final Button btnCancel =
                            new Button("Anular");

                    private final javafx.scene.layout.HBox buttons =
                            new javafx.scene.layout.HBox(8);

                    {
                        buttons.setAlignment(
                                javafx.geometry.Pos.CENTER
                        );

                        buttons.getChildren().addAll(
                                btnEdit,
                                btnCancel
                        );

                        btnEdit.setOnAction(event -> {

                            Consumption consumption =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleEditConsumption(consumption);
                        });

                        btnCancel.setOnAction(event -> {

                            Consumption consumption =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleCancelConsumption(consumption);
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        setGraphic(
                                empty ? null : buttons
                        );
                    }
                }
        );
    }

    private void handleEditConsumption(
            Consumption consumption) {

        if (consumption == null) {
            return;
        }

        consumptionBeingEdited = consumption;

        // Seleccionar tipo
        if (consumption.getIdConsumptionType() == 1) {

            cmbConsumptionType.setValue("Producto");

            for (Product product : cmbProduct.getItems()) {

                if (product.getIdProduct()
                        == consumption.getIdProduct()) {

                    cmbProduct.setValue(product);
                    break;
                }
            }

        } else if (consumption.getIdConsumptionType() == 2) {

            cmbConsumptionType.setValue("Servicio");

            for (Service service : cmbService.getItems()) {

                if (service.getIdService()
                        == consumption.getIdService()) {

                    cmbService.setValue(service);
                    break;
                }
            }
        }

        // Cantidad
        txtQuantity.setText(
                String.valueOf(
                        consumption.getQuantity()
                )
        );

        // Cambiar texto del botón
        btnAdd.setText("Guardar cambios");
    }

    private void handleUpdateConsumption() {

        if (consumptionBeingEdited == null) {
            return;
        }

        String quantityText =
                txtQuantity.getText().trim();

        if (quantityText.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Cantidad",
                    "Debe ingresar una cantidad."
            );

            return;
        }

        int quantity;

        try {

            quantity = Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Cantidad",
                    "La cantidad debe ser un número entero."
            );

            return;
        }

        if (quantity <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Cantidad",
                    "La cantidad debe ser mayor a cero."
            );

            return;
        }

        if (quantity > 30) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Cantidad máxima",
                    "La cantidad máxima por consumo es de 30 unidades."
            );

            return;
        }

        BigDecimal unitPrice;

        if ("Producto".equals(
                cmbConsumptionType.getValue())) {

            Product product =
                    cmbProduct.getValue();

            if (product == null) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Producto",
                        "Debe seleccionar un producto."
                );

                return;
            }

            consumptionBeingEdited.setIdConsumptionType(1);
            consumptionBeingEdited.setIdProduct(
                    product.getIdProduct()
            );
            consumptionBeingEdited.setIdService(0);

            unitPrice = product.getPrice();

        } else {

            Service service =
                    cmbService.getValue();

            if (service == null) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Servicio",
                        "Debe seleccionar un servicio."
                );

                return;
            }

            consumptionBeingEdited.setIdConsumptionType(2);
            consumptionBeingEdited.setIdProduct(0);
            consumptionBeingEdited.setIdService(
                    service.getIdService()
            );

            unitPrice = service.getPrice();
        }

        BigDecimal total =
                unitPrice.multiply(
                        BigDecimal.valueOf(quantity)
                );

        consumptionBeingEdited.setQuantity(quantity);
        consumptionBeingEdited.setUnitPrice(unitPrice);
        consumptionBeingEdited.setTotal(total);

        try (
                java.sql.Connection conn =
                        repositories.ConexionDB.getConnection()
        ) {

            boolean updated =
                    consumptionRepo.updateConsumption(
                            conn,
                            consumptionBeingEdited
                    );

            if (!updated) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Error",
                        "No se pudo actualizar el consumo."
                );

                return;
            }

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Ocurrió un error al actualizar el consumo."
            );

            e.printStackTrace();

            return;
        }

        consumptionBeingEdited = null;

        btnAdd.setText("Agregar consumo");

        cmbConsumptionType.setValue(null);
        cmbProduct.setValue(null);
        cmbService.setValue(null);
        txtQuantity.clear();

        cmbProduct.setDisable(true);
        cmbService.setDisable(true);

        loadConsumptions();

        showAlert(
                Alert.AlertType.INFORMATION,
                "Consumo actualizado",
                "El consumo se actualizó correctamente."
        );
    }

    private void handleCancelConsumption(
            Consumption consumption) {

        if (consumption == null) {
            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Anular consumo");
        confirmation.setHeaderText(null);
        confirmation.setContentText(
                "¿Está seguro de que desea anular este consumo?"
        );

        ButtonType btnYes =
                new ButtonType("Sí, anular");

        ButtonType btnNo =
                new ButtonType(
                        "Cancelar",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        confirmation.getButtonTypes().setAll(
                btnYes,
                btnNo
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty()
                || result.get() != btnYes) {

            return;
        }

        try (
                java.sql.Connection conn =
                        repositories.ConexionDB.getConnection()
        ) {

            boolean cancelled =
                    consumptionRepo.anularConsumption(
                            conn,
                            consumption.getIdConsumption()
                    );

            if (!cancelled) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Error",
                        "No se pudo anular el consumo."
                );

                return;
            }

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Ocurrió un error al anular el consumo."
            );

            e.printStackTrace();

            return;
        }

        loadConsumptions();

        showAlert(
                Alert.AlertType.INFORMATION,
                "Consumo anulado",
                "El consumo se anuló correctamente."
        );
    }

    // =========================================================
    // CALCULAR TOTAL
    // =========================================================

    private void calculateTotal() {

        BigDecimal total = BigDecimal.ZERO;

        for (Consumption consumption : consumptions) {

            if (consumption.getTotal() != null) {

                total = total.add(
                        consumption.getTotal()
                );
            }
        }

        txtTotal.setText(
                total.toString()
        );
    }

    // =========================================================
    // TIPO DE CONSUMO
    // =========================================================

    private void setupConsumptionType() {

        cmbConsumptionType
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    cmbProduct.setValue(null);
                    cmbService.setValue(null);

                    if ("Producto".equals(newValue)) {

                        cmbProduct.setDisable(false);
                        cmbService.setDisable(true);

                    } else if ("Servicio".equals(newValue)) {

                        cmbProduct.setDisable(true);
                        cmbService.setDisable(false);

                    } else {

                        cmbProduct.setDisable(true);
                        cmbService.setDisable(true);
                    }
                });
    }

    // =========================================================
    // RECIBIR RESERVA
    // =========================================================

    public void setReservation(Reservation reservation) {

        this.reservation = reservation;

        if (reservation != null) {

            lblReservation.setText(
                    "Reserva N.º "
                            + reservation.getIdReservation()
            );

            loadConsumptions();
        }
    }

    public void setDashboardController(
            DashboardController dashboardController) {

        this.dashboardController = dashboardController;
    }

    // =========================================================
    // AGREGAR CONSUMO
    // =========================================================

    private void handleAddConsumption() {

        if (consumptionBeingEdited != null) {
            handleUpdateConsumption();
            return;
        }

        // 1. Verificar que exista una reserva
        if (reservation == null) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se encontró la reserva."
            );

            return;
        }

        // 2. Obtener tipo seleccionado
        String selectedType =
                cmbConsumptionType.getValue();

        if (selectedType == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Tipo de consumo",
                    "Debe seleccionar el tipo de consumo."
            );

            return;
        }

        // 3. Obtener y validar cantidad
        String quantityText =
                txtQuantity.getText().trim();

        if (quantityText.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Cantidad",
                    "Debe ingresar una cantidad."
            );

            return;
        }

        int quantity;

        try {

            quantity = Integer.parseInt(quantityText);

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Cantidad",
                    "La cantidad debe ser un número entero."
            );

            return;
        }

        if (quantity <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Cantidad",
                    "La cantidad debe ser mayor a cero."
            );

            return;
        }

        if (quantity > 30) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Cantidad máxima",
                    "La cantidad máxima por consumo es de 30 unidades."
            );

            return;
        }

        // 4. Variables del consumo
        int idConsumptionType;
        int idProduct = 0;
        int idService = 0;
        BigDecimal unitPrice;

        // 5. PRODUCTO
        if ("Producto".equals(selectedType)) {

            Product product =
                    cmbProduct.getValue();

            if (product == null) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Producto",
                        "Debe seleccionar un producto."
                );

                return;
            }

            idConsumptionType = 1;
            idProduct = product.getIdProduct();
            unitPrice = product.getPrice();

            // 6. SERVICIO
        } else {

            Service service =
                    cmbService.getValue();

            if (service == null) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Servicio",
                        "Debe seleccionar un servicio."
                );

                return;
            }

            idConsumptionType = 2;
            idService = service.getIdService();
            unitPrice = service.getPrice();
        }

        // 7. Calcular total
        BigDecimal total =
                unitPrice.multiply(
                        BigDecimal.valueOf(quantity)
                );

        // 8. Crear objeto Consumption
        Consumption consumption =
                new Consumption(
                        reservation.getIdReservation(),
                        idConsumptionType,
                        idProduct,
                        idService,
                        quantity,
                        unitPrice,
                        total,
                        java.time.LocalDateTime.now(),
                        1,
                        null
                );

        // 9. Guardar en BD
        try (
                java.sql.Connection conn =
                        repositories.ConexionDB.getConnection()
        ) {

            boolean saved =
                    consumptionRepo.createConsumption(
                            conn,
                            consumption
                    );

            if (!saved) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Error",
                        "No se pudo guardar el consumo."
                );

                return;
            }

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    "Ocurrió un error al guardar el consumo."
            );

            e.printStackTrace();

            return;
        }

        // 10. Recargar tabla
        loadConsumptions();

        // 11. Limpiar campos
        cmbConsumptionType.setValue(null);
        cmbProduct.setValue(null);
        cmbService.setValue(null);
        txtQuantity.clear();

        cmbProduct.setDisable(true);
        cmbService.setDisable(true);

        showAlert(
                Alert.AlertType.INFORMATION,
                "Consumo agregado",
                "El consumo se agregó correctamente."
        );
    }

    // =========================================================
    // CERRAR
    // =========================================================

    private void closeWindow() {

        if (dashboardController != null) {

            dashboardController.loadView(
                    "/views/reservations.fxml"
            );

        } else {

            System.err.println(
                    "DashboardController is not connected."
            );
        }
    }

    // =========================================================
    // ALERTAS
    // =========================================================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}

