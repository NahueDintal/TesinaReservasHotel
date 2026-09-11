package controllers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.*;
import repositories.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NewReservationController {
    // RESERVA
    @FXML private TextField txtCustomerSearch;
    @FXML private ListView<Customer> lstCustomers;
    private final ObservableList<Customer> activeCustomers = FXCollections.observableArrayList();
    private Customer selectedCustomer;
    @FXML private DatePicker dpCheckIn;
    @FXML private DatePicker dpCheckOut;
    @FXML private TextField txtNumberOfGuests;
    @FXML private TextField txtTotalRate;
    @FXML private ComboBox<ReservationStatus> cmbReservationStatus;
    @FXML private ComboBox<ReservationType> cmbReservationType;
    @FXML private TextArea txtReservationObservations;
    // PAYMENT
    @FXML private TextField txtPaymentAmount;
    @FXML private DatePicker dpPaymentDate;
    @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;
    @FXML private ComboBox<PaymentStatus> cmbPaymentStatus;
    @FXML private TextArea txtPaymentObservations;
    // CONSUMPTION
    @FXML private ComboBox<Integer> cmbConsumptionType;
    @FXML private ComboBox<Product> cmbProduct;
    @FXML private ComboBox<Service> cmbService;
    @FXML private TextField txtConsumptionQuantity;
    @FXML private TableView<Consumption> tblConsumptions;
    @FXML private TableColumn<Consumption, Integer> colConsumptionQuantity;
    @FXML private TableColumn<Consumption, BigDecimal> colConsumptionUnitPrice;
    @FXML private TableColumn<Consumption, BigDecimal> colConsumptionTotal;
    @FXML private TableColumn<Consumption, String> colConsumptionType;
    @FXML private TableColumn<Consumption, String> colConsumptionName;
    @FXML private TextField txtConsumptionTotal;
    // REPOSITORIES
    private DashboardController dashboardController;
    private Reservation reservationToEdit;
    private final ReservationRepo reservationRepo;
    private final ReservationStatusRepo reservationStatusRepo;
    private final ReservationTypeRepo reservationTypeRepo;
    private final PaymentRepo paymentRepo;
    private final PaymentMethodRepo paymentMethodRepo;
    private final PaymentStatusRepo paymentStatusRepo;
    private final ConsumptionRepo consumptionRepo;
    private final ProductRepo productRepo;
    private final ServiceRepo serviceRepo;
    private final CustomerDAO customerDAO;
    // LISTA DE CONSUMOS
    private final ObservableList<Consumption> consumptions = FXCollections.observableArrayList();
    private final List<Consumption> modifiedConsumptions = new java.util.ArrayList<>();
    private Consumption consumptionBeingEdited = null;
    // CONSTRUCTOR
    public NewReservationController() {
        reservationRepo = new ReservationRepo();
        reservationStatusRepo = new ReservationStatusRepo();
        reservationTypeRepo = new ReservationTypeRepo();
        paymentRepo = new PaymentRepo();
        paymentMethodRepo = new PaymentMethodRepo();
        paymentStatusRepo = new PaymentStatusRepo();
        consumptionRepo = new ConsumptionRepo();
        productRepo = new ProductRepo();
        serviceRepo = new ServiceRepo();
        customerDAO = new CustomerDAO();
    }
    // RECIBIR RESERVA A MODIFICAR
    public void setReservationToEdit(
            Reservation reservation) {
        this.reservationToEdit = reservation;
        if (reservation == null) {
            return;
        }
        // CLIENTE
        try {
            Customer customer =
                    customerDAO.searchById(
                            reservation.getIdCustomer()
                    );
            if (customer != null) {
                selectedCustomer = customer;

                txtCustomerSearch.setText(customer.getName() + " " + customer.getSurname());
            }
        } catch (Exception e) {
            System.err.println(
                    "Error cargando el cliente de la reserva: "
                            + e.getMessage()
            );
        }
        // DATOS RESERVA
        dpCheckIn.setValue(
                reservation.getCheckIn()
        );
        dpCheckOut.setValue(
                reservation.getCheckOut()
        );
        txtNumberOfGuests.setText(
                String.valueOf(
                        reservation.getNumberOfGuests()
                )
        );
        txtTotalRate.setText(
                reservation.getTotalRate() != null
                        ? reservation.getTotalRate().toString()
                        : ""
        );
        // ESTADO RESERVA
        cmbReservationStatus.getItems()
                .stream()
                .filter(status ->
                        status.getIdReservationStatus()
                                == reservation.getIdReservationStatus()
                )
                .findFirst()
                .ifPresent(
                        cmbReservationStatus::setValue
                );
        // TIPO RESERVA
        cmbReservationType.getItems()
                .stream()
                .filter(type ->
                        type.getIdReservationType()
                                == reservation.getIdReservationType()
                )
                .findFirst()
                .ifPresent(
                        cmbReservationType::setValue
                );
        // OBSERVACIONES
        txtReservationObservations.setText(
                reservation.getObservations() == null
                        ? ""
                        : reservation.getObservations()
        );
        // CARGAR PAGO
        loadReservationPayment(
                reservation.getIdReservation()
        );
        // CARGAR CONSUMOS
        loadReservationConsumptions(
                reservation.getIdReservation()
        );
    }
    // CARGAR PAGO DE RESERVA
    private void loadReservationPayment(
            int idReservation) {
        try {
            List<Payment> payments =
                    paymentRepo.getPaymentsByReservation(
                            idReservation
                    );
            if (payments.isEmpty()) {
                txtPaymentAmount.clear();
                dpPaymentDate.setValue(null);
                cmbPaymentMethod.setValue(null);
                cmbPaymentStatus.setValue(null);
                txtPaymentObservations.clear();
                return;
            }
            // Por ahora manejamos el primer pago
            Payment payment =
                    payments.get(0);
            txtPaymentAmount.setText(
                    payment.getAmount() != null
                            ? payment.getAmount().toString()
                            : ""
            );
            if (payment.getPaymentDate() != null) {
                dpPaymentDate.setValue(
                        payment.getPaymentDate().toLocalDate()
                );
            }
            cmbPaymentMethod.getItems()
                    .stream()
                    .filter(method ->
                            method.getIdPaymentMethod()
                                    == payment.getIdPaymentMethod()
                    )
                    .findFirst()
                    .ifPresent(
                            cmbPaymentMethod::setValue
                    );
            cmbPaymentStatus.getItems()
                    .stream()
                    .filter(status ->
                            status.getIdPaymentStatus()
                                    == payment.getIdPaymentStatus()
                    )
                    .findFirst()
                    .ifPresent(
                            cmbPaymentStatus::setValue
                    );
            txtPaymentObservations.setText(
                    payment.getObservations() == null
                            ? ""
                            : payment.getObservations()
            );
        } catch (Exception e) {
            System.err.println(
                    "Error cargando el pago de la reserva: "
                            + e.getMessage()
            );
            e.printStackTrace();
        }
    }
    // CARGAR CONSUMOS DE RESERVA
    private void loadReservationConsumptions(
            int idReservation) {
        try {
            List<Consumption> consumptionsBD =
                    consumptionRepo.getConsumptionsByReservation(
                            idReservation
                    );
            consumptions.clear();
            consumptions.addAll(
                    consumptionsBD
            );
            tblConsumptions.refresh();
            updateConsumptionTotal();
            System.out.println(
                    "Consumos cargados: "
                            + consumptions.size()
            );
        } catch (Exception e) {
            System.err.println(
                    "Error cargando los consumos de la reserva: "
                            + e.getMessage()
            );
            e.printStackTrace();
        }
    }
    // INITIALIZE
    @FXML
    public void initialize() {
        System.out.println(
                "NewReservationController iniciado"
        );
        // TABLA CONSUMOS
        configureConsumptionTable();
        // TIPO CONSUMO
        cmbConsumptionType.setItems(
                FXCollections.observableArrayList(
                        1,
                        2
                )
        );
        cmbConsumptionType.setOnAction(event -> {
            Integer typeValue =
                    cmbConsumptionType.getValue();
            if (typeValue == null) {
                cmbProduct.setDisable(true);
                cmbService.setDisable(true);
                return;
            }
            if (typeValue == 1) {
                // Producto
                cmbProduct.setDisable(false);
                cmbService.setDisable(true);
                cmbService.setValue(null);
            } else if (typeValue == 2) {
                // Servicio
                cmbProduct.setDisable(true);
                cmbService.setDisable(false);
                cmbProduct.setValue(null);
            }
        });
        // CARGAR COMBOS
        loadReservationStatuses();
        loadReservationTypes();
        loadPaymentMethods();
        loadPaymentStatuses();
        loadProducts();
        loadServices();
        loadCustomers();
        configureCustomerSearch();
        // CONSUMOS
        txtConsumptionTotal.setText("0.00");
        txtConsumptionTotal.setEditable(false);
        cmbProduct.setDisable(true);
        cmbService.setDisable(true);
    }
    // DASHBOARD
    public void setDashboardController(
            DashboardController dashboardController) {
        this.dashboardController =
                dashboardController;
    }
    // CUSTOMERS
    private void loadCustomers() {
        try {
            List<Customer> customers = customerDAO.listAll();

            activeCustomers.clear();

            for (Customer customer : customers) {
                if (customer.getIdCustomerStatus() == 1) {
                    activeCustomers.add(customer);
                }
            }

            lstCustomers.setItems(activeCustomers);

            System.out.println(
                    "Clientes activos encontrados: " +
                            activeCustomers.size()
            );

        } catch (Exception e) {
            System.err.println("Error loading customers:");
            e.printStackTrace();
        }
    }
    private void configureCustomerSearch() {

        txtCustomerSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    String text = newValue.trim().toLowerCase();

                    if (text.isEmpty()) {
                        lstCustomers.setItems(activeCustomers);
                        return;
                    }

                    ObservableList<Customer> filtered =
                            FXCollections.observableArrayList();

                    for (Customer customer : activeCustomers) {

                        String name =
                                customer.getName().toLowerCase();

                        String surname =
                                customer.getSurname().toLowerCase();

                        String document =
                                customer.getDocumentNumber() != null
                                        ? customer.getDocumentNumber().toLowerCase()
                                        : "";

                        if (name.contains(text)
                                || surname.contains(text)
                                || document.contains(text)) {

                            filtered.add(customer);
                        }
                    }

                    lstCustomers.setItems(filtered);
                }
        );

        lstCustomers.setOnMouseClicked(event -> {
            Customer customer =
                    lstCustomers.getSelectionModel()
                            .getSelectedItem();

            if (customer != null) {
                selectedCustomer = customer;
                txtCustomerSearch.setText(
                        customer.getName() + " " +
                                customer.getSurname()
                );
                lstCustomers.setVisible(false);
                lstCustomers.setManaged(false);
            }
        });

        txtCustomerSearch.focusedProperty().addListener(
                (observable, oldValue, focused) -> {

                    if (focused) {
                        lstCustomers.setVisible(true);
                        lstCustomers.setManaged(true);
                    }
                }
        );
    }
    // RESERVATION STATUS
    private void loadReservationStatuses() {
        try {
            List<ReservationStatus> statuses =
                    reservationStatusRepo
                            .getReservationStatuses();
            cmbReservationStatus.getItems().clear();
            cmbReservationStatus.getItems().addAll(
                    statuses
            );
        } catch (Exception e) {
            System.err.println(
                    "Error cargando estados de reserva: "
            );
            e.printStackTrace();
        }
    } //?????
    // RESERVATION TYPE
    private void loadReservationTypes() {
        try {
            List<ReservationType> types =
                    reservationTypeRepo
                            .getReservationTypes();
            cmbReservationType.getItems().clear();
            cmbReservationType.getItems().addAll(
                    types
            );
        } catch (Exception e) {
            System.err.println(
                    "Error cargando tipos de reserva: "
            );
            e.printStackTrace();
        }
    }
    // PAYMENT METHOD
    private void loadPaymentMethods() {
        try {
            List<PaymentMethod> methods =
                    paymentMethodRepo
                            .getPaymentMethods();
            cmbPaymentMethod.getItems().clear();
            cmbPaymentMethod.getItems().addAll(
                    methods
            );
        } catch (Exception e) {
            System.err.println(
                    "Error cargando métodos de pago: "
            );
            e.printStackTrace();
        }
    }
    // PAYMENT STATUS
    private void loadPaymentStatuses() {
        try {
            List<PaymentStatus> statuses =
                    paymentStatusRepo
                            .getPaymentStatuses();
            cmbPaymentStatus.getItems().clear();
            cmbPaymentStatus.getItems().addAll(
                    statuses
            );
        } catch (Exception e) {
            System.err.println(
                    "Error cargando estados de pago: "
            );
            e.printStackTrace();
        }
    } ///??
    // PRODUCTS
    private void loadProducts() {
        try {
            List<Product> products =
                    productRepo.getActiveProducts();
            cmbProduct.getItems().clear();
            cmbProduct.getItems().addAll(
                    products
            );
        } catch (Exception e) {
            System.err.println(
                    "Error cargando productos: "
            );
            e.printStackTrace();
        }
    }
    // SERVICES
    private void loadServices() {
        try {
            List<Service> services =
                    serviceRepo.getActiveServices();
            cmbService.getItems().clear();
            cmbService.getItems().addAll(
                    services
            );
        } catch (Exception e) {
            System.err.println(
                    "Error cargando servicios: "
            );
            e.printStackTrace();
        }
    }
    // TABLA CONSUMOS
    private void configureConsumptionTable() {
        colConsumptionQuantity.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );
        colConsumptionUnitPrice.setCellValueFactory(
                new PropertyValueFactory<>("unitPrice")
        );
        colConsumptionTotal.setCellValueFactory(
                new PropertyValueFactory<>("total")
        );
        // TIPO
        colConsumptionType.setCellValueFactory(
                cellData -> {
                    Consumption consumption =
                            cellData.getValue();
                    String typeValue;
                    if (consumption.getIdConsumptionType() == 1) {
                        typeValue = "Producto";
                    } else if (
                            consumption.getIdConsumptionType() == 2) {
                        typeValue = "Servicio";
                    } else {
                        typeValue = "Desconocido";
                    }
                    return new javafx.beans.property
                            .SimpleStringProperty(typeValue);
                }
        );
        // NOMBRE
        colConsumptionName.setCellValueFactory(
                cellData -> {
                    Consumption consumption =
                            cellData.getValue();
                    String name = "";
                    if (consumption.getIdConsumptionType() == 1) {
                        for (Product product :
                                cmbProduct.getItems()) {
                            if (product.getIdProduct()
                                    == consumption.getIdProduct()) {
                                name =
                                        product.getName();
                                break;
                            }
                        }
                    } else if (
                            consumption.getIdConsumptionType() == 2) {
                        for (Service service :
                                cmbService.getItems()) {
                            if (service.getIdService()
                                    == consumption.getIdService()) {
                                name =
                                        service.getName();
                                break;
                            }
                        }
                    }
                    return new javafx.beans.property
                            .SimpleStringProperty(name);
                }
        );
        tblConsumptions.setItems(
                consumptions
        );
    }
    // AGREGAR CONSUMO
    @FXML
    private void handleAddConsumption() {
        Integer consumptionType =
                cmbConsumptionType.getValue();
        if (consumptionType == null) {
            showError(
                    "Debe seleccionar el tipo de consumo."
            );
            return;
        }
        String quantityText =
                txtConsumptionQuantity
                        .getText()
                        .trim();
        if (quantityText.isEmpty()) {
            showError(
                    "Debe ingresar la cantidad."
            );
            return;
        }
        int quantity;
        try {
            quantity =
                    Integer.parseInt(
                            quantityText
                    );
        } catch (NumberFormatException e) {
            showError(
                    "La cantidad debe contener solamente números."
            );
            return;
        }
        if (quantity <= 0) {
            showError(
                    "La cantidad debe ser mayor que 0."
            );
            return;
        }
        BigDecimal unitPrice;
        int idProduct = 0;
        int idService = 0;
        // PRODUCTO
        if (consumptionType == 1) {
            Product product =
                    cmbProduct.getValue();
            if (product == null) {
                showError(
                        "Debe seleccionar un producto."
                );
                return;
            }
            idProduct =
                    product.getIdProduct();
            unitPrice =
                    product.getPrice();
            // SERVICIO
        } else {
            Service service =
                    cmbService.getValue();
            if (service == null) {
                showError(
                        "Debe seleccionar un servicio."
                );
                return;
            }
            idService =
                    service.getIdService();
            unitPrice =
                    service.getPrice();
        }
        // TOTAL
        BigDecimal total =
                unitPrice.multiply(
                        BigDecimal.valueOf(quantity)
                );
        // CONSUMPTION
        Consumption consumption =
                new Consumption(
                        0,
                        consumptionType,
                        idProduct,
                        idService,
                        quantity,
                        unitPrice,
                        total,
                        LocalDateTime.now(),
                        1,
                        null
                );
        consumption.setIdConsumptionStatus(1);
        // AGREGAR A LA TABLA
        consumptions.add(
                consumption
        );
        updateConsumptionTotal();
        cmbProduct.setValue(null);
        cmbService.setValue(null);
        txtConsumptionQuantity.clear();
    }
    // MODIFICAR CONSUMO
    @FXML
    private void handleModifyConsumption() {
        // SI YA ESTAMOS EDITANDO → GUARDAR CAMBIOS
        if (consumptionBeingEdited != null) {
            Integer consumptionType =
                    cmbConsumptionType.getValue();
            if (consumptionType == null) {
                showError(
                        "Debe seleccionar el tipo de consumo."
                );
                return;
            }
            String quantityText =
                    txtConsumptionQuantity.getText().trim();
            if (quantityText.isEmpty()) {
                showError(
                        "Debe ingresar la cantidad."
                );
                return;
            }
            int quantity;
            try {
                quantity =
                        Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                showError(
                        "La cantidad debe contener solamente números."
                );
                return;
            }
            if (quantity <= 0) {
                showError(
                        "La cantidad debe ser mayor que 0."
                );
                return;
            }
            int idProduct = 0;
            int idService = 0;
            BigDecimal unitPrice;
            // PRODUCTO
            if (consumptionType == 1) {
                Product product =
                        cmbProduct.getValue();
                if (product == null) {
                    showError(
                            "Debe seleccionar un producto."
                    );
                    return;
                }
                idProduct =
                        product.getIdProduct();
                unitPrice =
                        product.getPrice();
                // SERVICIO
            } else {
                Service service =
                        cmbService.getValue();
                if (service == null) {
                    showError(
                            "Debe seleccionar un servicio."
                    );
                    return;
                }
                idService =
                        service.getIdService();
                unitPrice =
                        service.getPrice();
            }
            // CALCULAR TOTAL
            BigDecimal total =
                    unitPrice.multiply(
                            BigDecimal.valueOf(quantity)
                    );
            // ACTUALIZAR OBJETO
            consumptionBeingEdited.setIdConsumptionType(
                    consumptionType
            );
            consumptionBeingEdited.setIdProduct(
                    idProduct
            );
            consumptionBeingEdited.setIdService(
                    idService
            );
            consumptionBeingEdited.setQuantity(
                    quantity
            );
            consumptionBeingEdited.setUnitPrice(
                    unitPrice
            );
            consumptionBeingEdited.setTotal(
                    total
            );
            if (consumptionBeingEdited.getIdConsumption() > 0 &&
                    !modifiedConsumptions.contains(consumptionBeingEdited)) {
                modifiedConsumptions.add(
                        consumptionBeingEdited
                );
            }
            // ACTUALIZAR TABLA
            tblConsumptions.refresh();
            updateConsumptionTotal();
            // TERMINAR EDICIÓN
            consumptionBeingEdited = null;
            cmbConsumptionType.setValue(null);
            cmbProduct.setValue(null);
            cmbService.setValue(null);
            txtConsumptionQuantity.clear();
            cmbProduct.setDisable(true);
            cmbService.setDisable(true);
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Consumo",
                    "El consumo fue modificado correctamente."
            );
            return;
        }
        // COMENZAR EDICIÓN
        Consumption selected =
                tblConsumptions.getSelectionModel()
                        .getSelectedItem();
        if (selected == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Consumo",
                    "Seleccione un consumo para modificar."
            );
            return;
        }
        // NO PERMITIR MODIFICAR CONSUMOS ANULADOS
        if (selected.getIdConsumptionStatus() != 1) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Consumo",
                    "No se puede modificar un consumo que está anulado."
            );
            return;
        }
        // CARGAR DATOS EN EL FORMULARIO
        cmbConsumptionType.setValue(
                selected.getIdConsumptionType()
        );
        txtConsumptionQuantity.setText(
                String.valueOf(
                        selected.getQuantity()
                )
        );
        if (selected.getIdConsumptionType() == 1) {
            Product product =
                    cmbProduct.getItems()
                            .stream()
                            .filter(p ->
                                    p.getIdProduct()
                                            == selected.getIdProduct()
                            )
                            .findFirst()
                            .orElse(null);
            cmbProduct.setValue(product);
            cmbService.setValue(null);
            cmbProduct.setDisable(false);
            cmbService.setDisable(true);
        } else {
            Service service =
                    cmbService.getItems()
                            .stream()
                            .filter(s ->
                                    s.getIdService()
                                            == selected.getIdService()
                            )
                            .findFirst()
                            .orElse(null);
            cmbService.setValue(service);
            cmbProduct.setValue(null);
            cmbProduct.setDisable(true);
            cmbService.setDisable(false);
        }
        // GUARDAR CONSUMO EN EDICIÓN
        consumptionBeingEdited = selected;
        showAlert(
                Alert.AlertType.INFORMATION,
                "Modificar consumo",
                "Modifique los datos y presione nuevamente " +
                        "\"Modificar consumo\" para aplicar los cambios."
        );
    }
    // TOTAL CONSUMOS
    private void updateConsumptionTotal() {
        BigDecimal total =
                BigDecimal.ZERO;
        for (Consumption consumption :
                consumptions) {
            if (consumption.getTotal() != null) {
                total =
                        total.add(
                                consumption.getTotal()
                        );
            }
        }
        txtConsumptionTotal.setText(
                total.toString()
        );
    }
    // Borrar consumo
    @FXML
    private void handleDeleteConsumption() {
        Consumption selected =
                tblConsumptions.getSelectionModel()
                        .getSelectedItem();
        if (selected == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Consumo",
                    "Seleccione un consumo para anular."
            );
            return;
        }
        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Anular consumo");
        confirmation.setHeaderText(
                "¿Está seguro de anular este consumo?"
        );
        confirmation.setContentText(
                "El consumo no será eliminado de la base de datos."
        );
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Acá posteriormente llamaremos al repository
                // para hacer el soft delete.
            }
        });
    }
    // GUARDAR
    @FXML
    private void handleSave() {
        Connection conn = null;
        try {
            List<String> errors = new ArrayList<>();

            Customer customer = selectedCustomer;
            int idCustomer = 0;

            if (customer == null) {
                errors.add("Debe seleccionar un cliente.");
            } else {
                idCustomer = customer.getIdCustomer();
            }

            LocalDate checkIn = dpCheckIn.getValue();
            LocalDate checkOut = dpCheckOut.getValue();
            if (checkIn == null) errors.add("Debe seleccionar la fecha de check-in.");
            if (checkOut == null) errors.add("Debe seleccionar la fecha de check-out.");
            if (checkIn != null && checkOut != null && !checkOut.isAfter(checkIn)) {
                errors.add("La fecha de check-out debe ser posterior a la fecha de check-in.");
            }

            String guestsText = txtNumberOfGuests.getText().trim();
            int numberOfGuests = 0;
            if (guestsText.isEmpty()) {
                errors.add("Debe ingresar la cantidad de huéspedes.");
            } else {
                try {
                    numberOfGuests = Integer.parseInt(guestsText);
                    if (numberOfGuests <= 0) errors.add("La cantidad de huéspedes debe ser mayor que 0.");
                } catch (NumberFormatException e) {
                    errors.add("La cantidad de huéspedes debe contener solamente números.");
                }
            }

            String rateText = txtTotalRate.getText().trim();
            BigDecimal totalRate = null;
            if (rateText.isEmpty()) {
                errors.add("Debe ingresar la tarifa total.");
            } else {
                try {
                    totalRate = new BigDecimal(rateText);
                    if (totalRate.compareTo(BigDecimal.ZERO) < 0) errors.add("La tarifa total no puede ser negativa.");
                } catch (NumberFormatException e) {
                    errors.add("La tarifa debe contener solamente números.");
                }
            }

            ReservationStatus reservationStatus = cmbReservationStatus.getValue();
            if (reservationStatus == null) errors.add("Debe seleccionar el estado de la reserva.");

            ReservationType reservationType = cmbReservationType.getValue();
            if (reservationType == null) errors.add("Debe seleccionar el tipo de reserva.");

            String reservationObservations = txtReservationObservations.getText();
            if (reservationObservations != null && reservationObservations.trim().isEmpty()) reservationObservations = null;

            String paymentText = txtPaymentAmount.getText().trim();
            BigDecimal paymentAmount = null;
            LocalDateTime paymentDate = null;
            PaymentMethod paymentMethod = null;
            PaymentStatus paymentStatus = null;
            String paymentObservations = null;

            if (!paymentText.isEmpty()) {
                try {
                    paymentAmount = new BigDecimal(paymentText);
                    if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) errors.add("El importe del pago debe ser mayor que 0.");
                } catch (NumberFormatException e) {
                    errors.add("El importe del pago debe contener solamente números.");
                }

                if (dpPaymentDate.getValue() == null) errors.add("Debe seleccionar la fecha del pago.");
                else paymentDate = dpPaymentDate.getValue().atStartOfDay();

                paymentMethod = cmbPaymentMethod.getValue();
                if (paymentMethod == null) errors.add("Debe seleccionar el método de pago.");

                paymentStatus = cmbPaymentStatus.getValue();
                if (paymentStatus == null) errors.add("Debe seleccionar el estado del pago.");

                paymentObservations = txtPaymentObservations.getText();
                if (paymentObservations != null && paymentObservations.trim().isEmpty()) paymentObservations = null;
            }

            if (!errors.isEmpty()) {
                if (errors.size() == 1) showError(errors.get(0));
                else showErrors(errors);
                return;
            }

            Reservation reservation = new Reservation(
                    idCustomer, LocalDateTime.now(), checkIn, checkOut,
                    reservationStatus.getIdReservationStatus(),
                    reservationType.getIdReservationType(), numberOfGuests,
                    totalRate, reservationObservations
            );

            conn = ConexionDB.getConnection();
            if (conn == null) {
                showError("No se pudo establecer conexión con la base de datos.");
                return;
            }
            conn.setAutoCommit(false);

            int idReservation;
            if (reservationToEdit == null) {
                idReservation = reservationRepo.createReservation(conn, reservation);
            } else {
                reservation.setIdReservation(reservationToEdit.getIdReservation());
                if (!reservationRepo.updateReservation(conn, reservation)) {
                    conn.rollback();
                    showError("No se pudo actualizar la reserva.");
                    return;
                }
                idReservation = reservationToEdit.getIdReservation();
            }

            if (idReservation <= 0) {
                conn.rollback();
                showError(reservationToEdit == null ? "No se pudo crear la reserva." : "No se pudo actualizar la reserva.");
                return;
            }

            // Crear consumos nuevos. Los existentes tienen idConsumption > 0.
            for (Consumption consumption : consumptions) {
                if (consumption.getIdConsumption() == 0) {
                    consumption.setIdReservation(idReservation);
                    if (!consumptionRepo.createConsumption(conn, consumption)) {
                        conn.rollback();
                        showError("No se pudo registrar uno de los consumos.");
                        return;
                    }
                }
            }

            // Actualizar consumos modificados.
            for (Consumption consumption : modifiedConsumptions) {
                if (!consumptionRepo.updateConsumption(conn, consumption)) {
                    conn.rollback();
                    showError("No se pudo actualizar uno de los consumos.");
                    return;
                }
            }

            if (!paymentText.isEmpty()) {
                Payment payment = new Payment(
                        idReservation, paymentAmount, paymentDate,
                        paymentMethod.getIdPaymentMethod(),
                        paymentStatus.getIdPaymentStatus(), paymentObservations
                );

                if (reservationToEdit != null) {
                    List<Payment> payments = paymentRepo.getPaymentsByReservation(idReservation);
                    if (!payments.isEmpty()) {
                        payment.setIdPayment(payments.get(0).getIdPayment());
                        if (!paymentRepo.updatePayment(conn, payment)) {
                            conn.rollback();
                            showError("No se pudo actualizar el pago.");
                            return;
                        }
                    } else if (!paymentRepo.createPayment(conn, payment)) {
                        conn.rollback();
                        showError("No se pudo registrar el pago.");
                        return;
                    }
                } else if (!paymentRepo.createPayment(conn, payment)) {
                    conn.rollback();
                    showError("No se pudo registrar el pago.");
                    return;
                }
            }

            conn.commit();
            showSuccess(
                    reservationToEdit == null
                            ? "La reserva se creó correctamente.\nNúmero de reserva: " + idReservation
                            : "La reserva se modificó correctamente.\nNúmero de reserva: " + idReservation
            );
            handleBack();
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackException) {
                System.err.println("Error al hacer rollback: " + rollbackException.getMessage());
            }
            System.err.println("Error SQL: " + e.getMessage());
            showError("Ocurrió un error en la transacción.");
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackException) {
                System.err.println("Error al hacer rollback: " + rollbackException.getMessage());
            }
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            showError("Ocurrió un error al guardar la reserva.");
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        if (dashboardController != null) {
            dashboardController.loadView(
                    "/views/reservations.fxml"
            );
        } else {
            System.err.println(
                    "DashboardController no está conectado."
            );
        }
    }
    // CANCELAR
    @FXML
    private void handleCancel() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancelar reserva");
        confirmation.setHeaderText("¿Está seguro de que desea cancelar?");
        confirmation.setContentText("Los datos ingresados se perderán.");

        ButtonType yesButton = new ButtonType("Sí");
        ButtonType noButton = new ButtonType("No");
        confirmation.getButtonTypes().setAll(yesButton, noButton);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                Alert secondConfirmation = new Alert(Alert.AlertType.CONFIRMATION);
                secondConfirmation.setTitle("Confirmar cancelación");
                secondConfirmation.setHeaderText("Todos los datos ingresados se perderán.");
                secondConfirmation.setContentText("¿Desea continuar?");

                ButtonType confirmButton = new ButtonType("Sí, cancelar");
                ButtonType backButton = new ButtonType("Volver");
                secondConfirmation.getButtonTypes().setAll(confirmButton, backButton);

                secondConfirmation.showAndWait().ifPresent(secondResponse -> {
                    if (secondResponse == confirmButton) {
                        handleBack();
                    }
                });
            }
        });
    }
    // LIMPIAR
    private void clearForm() {
        selectedCustomer = null;
        txtCustomerSearch.clear();
        lstCustomers.setVisible(false);
        lstCustomers.setManaged(false);
        dpCheckIn.setValue(null);
        dpCheckOut.setValue(null);
        txtNumberOfGuests.clear();
        txtTotalRate.clear();
        cmbReservationStatus.setValue(null);
        cmbReservationType.setValue(null);
        txtReservationObservations.clear();
        // PAYMENT
        txtPaymentAmount.clear();
        dpPaymentDate.setValue(null);
        cmbPaymentMethod.setValue(null);
        cmbPaymentStatus.setValue(null);
        txtPaymentObservations.clear();
        // CONSUMPTIONS
        consumptions.clear();
        modifiedConsumptions.clear();
        consumptionBeingEdited = null;
        if (tblConsumptions != null) {
            tblConsumptions.refresh();
        }
        updateConsumptionTotal();
        cmbConsumptionType.setValue(null);
        cmbProduct.setValue(null);
        cmbService.setValue(null);
        txtConsumptionQuantity.clear();
        txtConsumptionTotal.setText(
                "0.00"
        );
    }
    // ERROR
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("No se pudo guardar");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errores de validación");
        alert.setHeaderText("Revise los siguientes datos:");
        alert.setContentText("• " + String.join("\n• ", errors));
                alert.showAndWait();
    }
    // ÉXITO
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operación exitosa");
        alert.setHeaderText("Operación realizada correctamente");
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
