package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import models.*;
import repositories.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class NewReservationController {

    // =========================================================
    // RESERVA
    // =========================================================

    @FXML
    private ComboBox<Customer> cmbCustomer;

    @FXML
    private DatePicker dpCheckIn;

    @FXML
    private DatePicker dpCheckOut;

    @FXML
    private TextField txtNumberOfGuests;

    @FXML
    private TextField txtTotalRate;

    @FXML
    private ComboBox<ReservationStatus> cmbReservationStatus;

    @FXML
    private ComboBox<ReservationType> cmbReservationType;

    @FXML
    private TextArea txtReservationObservations;


    // =========================================================
    // PAYMENT
    // =========================================================

    @FXML
    private TextField txtPaymentAmount;

    @FXML
    private DatePicker dpPaymentDate;

    @FXML
    private ComboBox<PaymentMethod> cmbPaymentMethod;

    @FXML
    private ComboBox<PaymentStatus> cmbPaymentStatus;

    @FXML
    private TextArea txtPaymentObservations;


    // =========================================================
    // CONSUMPTION
    // =========================================================

    @FXML
    private ComboBox<Integer> cmbConsumptionType;

    @FXML
    private ComboBox<Product> cmbProduct;

    @FXML
    private ComboBox<Service> cmbService;

    @FXML
    private TextField txtConsumptionQuantity;

    @FXML
    private TableView<Consumption> tblConsumptions;

    @FXML
    private TableColumn<Consumption, Integer> colConsumptionQuantity;

    @FXML
    private TableColumn<Consumption, BigDecimal> colConsumptionUnitPrice;

    @FXML
    private TableColumn<Consumption, BigDecimal> colConsumptionTotal;

    @FXML
    private TableColumn<Consumption, String> colConsumptionType;

    @FXML
    private TableColumn<Consumption, String> colConsumptionName;

    @FXML
    private TextField txtConsumptionTotal;


    // =========================================================
    // REPOSITORIES
    // =========================================================

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


    // =========================================================
    // LISTA DE CONSUMOS
    // =========================================================

    private final ObservableList<Consumption> consumptions =
            FXCollections.observableArrayList();


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public NewReservationController() {

        reservationRepo = new ReservationRepo();

        reservationStatusRepo =
                new ReservationStatusRepo();

        reservationTypeRepo =
                new ReservationTypeRepo();

        paymentRepo =
                new PaymentRepo();

        paymentMethodRepo =
                new PaymentMethodRepo();

        paymentStatusRepo =
                new PaymentStatusRepo();

        consumptionRepo =
                new ConsumptionRepo();

        productRepo =
                new ProductRepo();

        serviceRepo =
                new ServiceRepo();

        customerDAO =
                new CustomerDAO();
    }


    // =========================================================
    // RECIBIR RESERVA A MODIFICAR
    // =========================================================

    public void setReservationToEdit(
            Reservation reservation) {

        this.reservationToEdit = reservation;

        if (reservation == null) {
            return;
        }

        // =====================================================
        // CLIENTE
        // =====================================================

        try {

            Customer customer =
                    customerDAO.searchById(
                            reservation.getIdCustomer()
                    );

            if (customer != null) {

                cmbCustomer.setValue(customer);
            }

        } catch (Exception e) {

            System.err.println(
                    "Error cargando el cliente de la reserva: "
                            + e.getMessage()
            );
        }


        // =====================================================
        // DATOS RESERVA
        // =====================================================

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


        // =====================================================
        // ESTADO RESERVA
        // =====================================================

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


        // =====================================================
        // TIPO RESERVA
        // =====================================================

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


        // =====================================================
        // OBSERVACIONES
        // =====================================================

        txtReservationObservations.setText(
                reservation.getObservations() == null
                        ? ""
                        : reservation.getObservations()
        );


        // =====================================================
        // CARGAR PAGO
        // =====================================================

        cargarPagoDeReserva(
                reservation.getIdReservation()
        );


        // =====================================================
        // CARGAR CONSUMOS
        // =====================================================

        cargarConsumosDeReserva(
                reservation.getIdReservation()
        );
    }


    // =========================================================
    // CARGAR PAGO DE RESERVA
    // =========================================================

    private void cargarPagoDeReserva(
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


    // =========================================================
    // CARGAR CONSUMOS DE RESERVA
    // =========================================================

    private void cargarConsumosDeReserva(
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


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        System.out.println(
                "NewReservationController iniciado"
        );


        // =====================================================
        // TABLA CONSUMOS
        // =====================================================

        configureConsumptionTable();


        // =====================================================
        // TIPO CONSUMO
        // =====================================================

        cmbConsumptionType.setItems(
                FXCollections.observableArrayList(
                        1,
                        2
                )
        );


        cmbConsumptionType.setOnAction(event -> {

            Integer tipo =
                    cmbConsumptionType.getValue();

            if (tipo == null) {

                cmbProduct.setDisable(true);
                cmbService.setDisable(true);

                return;
            }


            if (tipo == 1) {

                // Producto

                cmbProduct.setDisable(false);

                cmbService.setDisable(true);

                cmbService.setValue(null);

            } else if (tipo == 2) {

                // Servicio

                cmbProduct.setDisable(true);

                cmbService.setDisable(false);

                cmbProduct.setValue(null);
            }
        });


        // =====================================================
        // CARGAR COMBOS
        // =====================================================

        loadReservationStatuses();

        loadReservationTypes();

        loadPaymentMethods();

        loadPaymentStatuses();

        loadProducts();

        loadServices();

        loadCustomers();


        // =====================================================
        // CONSUMOS
        // =====================================================

        txtConsumptionTotal.setText("0.00");

        txtConsumptionTotal.setEditable(false);

        cmbProduct.setDisable(true);

        cmbService.setDisable(true);
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    public void setDashboardController(
            DashboardController dashboardController) {

        this.dashboardController =
                dashboardController;
    }


    // =========================================================
    // CUSTOMERS
    // =========================================================

    private void loadCustomers() {

        try {

            List<Customer> customers =
                    customerDAO.listAll();

            cmbCustomer.getItems().clear();

            cmbCustomer.getItems().addAll(
                    customers
            );

            System.out.println(
                    "Clientes encontrados: "
                            + customers.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error cargando clientes: "
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // RESERVATION STATUS
    // =========================================================

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
    }


    // =========================================================
    // RESERVATION TYPE
    // =========================================================

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


    // =========================================================
    // PAYMENT METHOD
    // =========================================================

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


    // =========================================================
    // PAYMENT STATUS
    // =========================================================

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
    }


    // =========================================================
    // PRODUCTS
    // =========================================================

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


    // =========================================================
    // SERVICES
    // =========================================================

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


    // =========================================================
    // TABLA CONSUMOS
    // =========================================================

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


        // =====================================================
        // TIPO
        // =====================================================

        colConsumptionType.setCellValueFactory(
                cellData -> {

                    Consumption consumption =
                            cellData.getValue();

                    String tipo;

                    if (consumption.getIdConsumptionType() == 1) {

                        tipo = "Producto";

                    } else if (
                            consumption.getIdConsumptionType() == 2) {

                        tipo = "Servicio";

                    } else {

                        tipo = "Desconocido";
                    }

                    return new javafx.beans.property
                            .SimpleStringProperty(tipo);
                }
        );


        // =====================================================
        // NOMBRE
        // =====================================================

        colConsumptionName.setCellValueFactory(
                cellData -> {

                    Consumption consumption =
                            cellData.getValue();

                    String nombre = "";


                    if (consumption.getIdConsumptionType() == 1) {

                        for (Product product :
                                cmbProduct.getItems()) {

                            if (product.getIdProduct()
                                    == consumption.getIdProduct()) {

                                nombre =
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

                                nombre =
                                        service.getName();

                                break;
                            }
                        }
                    }

                    return new javafx.beans.property
                            .SimpleStringProperty(nombre);
                }
        );


        tblConsumptions.setItems(
                consumptions
        );
    }


    // =========================================================
    // AGREGAR CONSUMO
    // =========================================================

    @FXML
    private void handleAddConsumption() {

        Integer consumptionType =
                cmbConsumptionType.getValue();

        if (consumptionType == null) {

            mostrarError(
                    "Debe seleccionar el tipo de consumo."
            );

            return;
        }


        String textoQuantity =
                txtConsumptionQuantity
                        .getText()
                        .trim();


        if (textoQuantity.isEmpty()) {

            mostrarError(
                    "Debe ingresar la cantidad."
            );

            return;
        }


        int quantity;

        try {

            quantity =
                    Integer.parseInt(
                            textoQuantity
                    );

        } catch (NumberFormatException e) {

            mostrarError(
                    "La cantidad debe contener solamente números."
            );

            return;
        }


        if (quantity <= 0) {

            mostrarError(
                    "La cantidad debe ser mayor que 0."
            );

            return;
        }


        BigDecimal unitPrice;

        int idProduct = 0;

        int idService = 0;


        // =====================================================
        // PRODUCTO
        // =====================================================

        if (consumptionType == 1) {

            Product product =
                    cmbProduct.getValue();

            if (product == null) {

                mostrarError(
                        "Debe seleccionar un producto."
                );

                return;
            }

            idProduct =
                    product.getIdProduct();

            unitPrice =
                    product.getPrice();


            // =====================================================
            // SERVICIO
            // =====================================================

        } else {

            Service service =
                    cmbService.getValue();

            if (service == null) {

                mostrarError(
                        "Debe seleccionar un servicio."
                );

                return;
            }

            idService =
                    service.getIdService();

            unitPrice =
                    service.getPrice();
        }


        // =====================================================
        // TOTAL
        // =====================================================

        BigDecimal total =
                unitPrice.multiply(
                        BigDecimal.valueOf(quantity)
                );


        // =====================================================
        // CONSUMPTION
        // =====================================================

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

        // =====================================================
        // AGREGAR A LA TABLA
        // =====================================================

        consumptions.add(
                consumption
        );

        updateConsumptionTotal();


        cmbProduct.setValue(null);

        cmbService.setValue(null);

        txtConsumptionQuantity.clear();
    }


    // =========================================================
    // TOTAL CONSUMOS
    // =========================================================

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

    // =========================================================
    // Borrar consumo
    // =========================================================

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

    // =========================================================
    // GUARDAR
    // =========================================================

    @FXML
    private void handleSave() {

        Connection conn = null;

        try {

            // =================================================
            // CLIENTE
            // =================================================

            Customer customer =
                    cmbCustomer.getValue();

            if (customer == null) {

                mostrarError(
                        "Debe seleccionar un cliente."
                );

                return;
            }


            int idCustomer =
                    customer.getIdCustomer();


            // =================================================
            // FECHAS
            // =================================================

            LocalDate checkIn =
                    dpCheckIn.getValue();

            LocalDate checkOut =
                    dpCheckOut.getValue();


            if (checkIn == null) {

                mostrarError(
                        "Debe seleccionar la fecha de check-in."
                );

                return;
            }


            if (checkOut == null) {

                mostrarError(
                        "Debe seleccionar la fecha de check-out."
                );

                return;
            }


            if (!checkOut.isAfter(checkIn)) {

                mostrarError(
                        "La fecha de check-out debe ser posterior " +
                                "a la fecha de check-in."
                );

                return;
            }


            // =================================================
            // HUÉSPEDES
            // =================================================

            String textoGuests =
                    txtNumberOfGuests
                            .getText()
                            .trim();

            if (textoGuests.isEmpty()) {

                mostrarError(
                        "Debe ingresar la cantidad de huéspedes."
                );

                return;
            }


            int numberOfGuests;

            try {

                numberOfGuests =
                        Integer.parseInt(
                                textoGuests
                        );

            } catch (NumberFormatException e) {

                mostrarError(
                        "La cantidad de huéspedes debe contener solamente números."
                );

                return;
            }


            if (numberOfGuests <= 0) {

                mostrarError(
                        "La cantidad de huéspedes debe ser mayor que 0."
                );

                return;
            }


            // =================================================
            // TARIFA
            // =================================================

            String textoRate =
                    txtTotalRate
                            .getText()
                            .trim();

            if (textoRate.isEmpty()) {

                mostrarError(
                        "Debe ingresar la tarifa total."
                );

                return;
            }


            BigDecimal totalRate;

            try {

                totalRate =
                        new BigDecimal(
                                textoRate
                        );

            } catch (NumberFormatException e) {

                mostrarError(
                        "La tarifa debe contener solamente números."
                );

                return;
            }


            if (totalRate.compareTo(
                    BigDecimal.ZERO) < 0) {

                mostrarError(
                        "La tarifa total no puede ser negativa."
                );

                return;
            }


            // =================================================
            // ESTADO
            // =================================================

            ReservationStatus reservationStatus =
                    cmbReservationStatus.getValue();

            if (reservationStatus == null) {

                mostrarError(
                        "Debe seleccionar el estado de la reserva."
                );

                return;
            }


            // =================================================
            // TIPO
            // =================================================

            ReservationType reservationType =
                    cmbReservationType.getValue();

            if (reservationType == null) {

                mostrarError(
                        "Debe seleccionar el tipo de reserva."
                );

                return;
            }


            // =================================================
            // OBSERVACIONES
            // =================================================

            String reservationObservations =
                    txtReservationObservations
                            .getText();


            if (reservationObservations != null &&
                    reservationObservations
                            .trim()
                            .isEmpty()) {

                reservationObservations = null;
            }


            // =================================================
            // RESERVATION
            // =================================================

            Reservation reservation =
                    new Reservation(
                            idCustomer,
                            LocalDateTime.now(),
                            checkIn,
                            checkOut,
                            reservationStatus
                                    .getIdReservationStatus(),
                            reservationType
                                    .getIdReservationType(),
                            numberOfGuests,
                            totalRate,
                            reservationObservations
                    );


            // =================================================
            // CONEXIÓN
            // =================================================

            conn =
                    ConexionDB.getConnection();


            if (conn == null) {

                mostrarError(
                        "No se pudo establecer conexión con la base de datos."
                );

                return;
            }


            conn.setAutoCommit(false);


            // =================================================
            // RESERVA
            // =================================================

            int idReservation;


            if (reservationToEdit == null) {

                // =============================================
                // NUEVA RESERVA
                // =============================================

                idReservation =
                        reservationRepo.createReservation(
                                conn,
                                reservation
                        );

            } else {

                // =============================================
                // MODIFICAR RESERVA
                // =============================================

                reservation.setIdReservation(
                        reservationToEdit
                                .getIdReservation()
                );


                boolean updated =
                        reservationRepo.updateReservation(
                                conn,
                                reservation
                        );


                if (!updated) {

                    conn.rollback();

                    mostrarError(
                            "No se pudo actualizar la reserva."
                    );

                    return;
                }


                idReservation =
                        reservationToEdit
                                .getIdReservation();
            }


            // =================================================
            // VALIDAR ID
            // =================================================

            if (idReservation <= 0) {

                conn.rollback();

                mostrarError(
                        reservationToEdit == null
                                ? "No se pudo crear la reserva."
                                : "No se pudo actualizar la reserva."
                );

                return;
            }


            // =================================================
            // CONSUMOS
            // =================================================

            /*
             * IMPORTANTE:
             *
             * Los consumos que ya estaban en BD tienen
             * idConsumption > 0.
             *
             * Los nuevos consumos agregados desde esta pantalla
             * tienen idConsumption = 0.
             *
             * Por eso solamente guardamos los nuevos.
             */

            for (Consumption consumption :
                    consumptions) {

                if (consumption.getIdConsumption() == 0) {

                    consumption.setIdReservation(
                            idReservation
                    );


                    boolean consumptionCreated =
                            consumptionRepo.createConsumption(
                                    conn,
                                    consumption
                            );


                    if (!consumptionCreated) {

                        conn.rollback();

                        mostrarError(
                                "No se pudo registrar uno de los consumos."
                        );

                        return;
                    }
                }
            }


            // =================================================
            // PAYMENT
            // =================================================

            String textoPayment =
                    txtPaymentAmount
                            .getText()
                            .trim();


            // El pago es opcional

            if (!textoPayment.isEmpty()) {

                BigDecimal paymentAmount;

                try {

                    paymentAmount =
                            new BigDecimal(
                                    textoPayment
                            );

                } catch (NumberFormatException e) {

                    conn.rollback();

                    mostrarError(
                            "El importe del pago debe contener solamente números."
                    );

                    return;
                }


                if (paymentAmount.compareTo(
                        BigDecimal.ZERO) <= 0) {

                    conn.rollback();

                    mostrarError(
                            "El importe del pago debe ser mayor que 0."
                    );

                    return;
                }


                if (dpPaymentDate.getValue() == null) {

                    conn.rollback();

                    mostrarError(
                            "Debe seleccionar la fecha del pago."
                    );

                    return;
                }


                PaymentMethod paymentMethod =
                        cmbPaymentMethod.getValue();

                if (paymentMethod == null) {

                    conn.rollback();

                    mostrarError(
                            "Debe seleccionar el método de pago."
                    );

                    return;
                }


                PaymentStatus paymentStatus =
                        cmbPaymentStatus.getValue();

                if (paymentStatus == null) {

                    conn.rollback();

                    mostrarError(
                            "Debe seleccionar el estado del pago."
                    );

                    return;
                }


                LocalDateTime paymentDate =
                        dpPaymentDate
                                .getValue()
                                .atStartOfDay();


                String paymentObservations =
                        txtPaymentObservations
                                .getText();


                if (paymentObservations != null &&
                        paymentObservations
                                .trim()
                                .isEmpty()) {

                    paymentObservations = null;
                }


                Payment payment =
                        new Payment(
                                idReservation,
                                paymentAmount,
                                paymentDate,
                                paymentMethod
                                        .getIdPaymentMethod(),
                                paymentStatus
                                        .getIdPaymentStatus(),
                                paymentObservations
                        );


                // =============================================
                // SI ES MODIFICACIÓN
                // =============================================

                if (reservationToEdit != null) {

                    List<Payment> payments =
                            paymentRepo
                                    .getPaymentsByReservation(
                                            idReservation
                                    );


                    if (!payments.isEmpty()) {

                        // Actualizar pago existente

                        Payment existingPayment =
                                payments.get(0);


                        payment.setIdPayment(
                                existingPayment
                                        .getIdPayment()
                        );


                        boolean paymentUpdated =
                                paymentRepo.updatePayment(
                                        conn,
                                        payment
                                );


                        if (!paymentUpdated) {

                            conn.rollback();

                            mostrarError(
                                    "No se pudo actualizar el pago."
                            );

                            return;
                        }

                    } else {

                        // No existía pago:
                        // crear uno nuevo

                        boolean paymentCreated =
                                paymentRepo.createPayment(
                                        conn,
                                        payment
                                );


                        if (!paymentCreated) {

                            conn.rollback();

                            mostrarError(
                                    "No se pudo registrar el pago."
                            );

                            return;
                        }
                    }


                } else {

                    // =========================================
                    // NUEVA RESERVA
                    // =========================================

                    boolean paymentCreated =
                            paymentRepo.createPayment(
                                    conn,
                                    payment
                            );


                    if (!paymentCreated) {

                        conn.rollback();

                        mostrarError(
                                "No se pudo registrar el pago."
                        );

                        return;
                    }
                }
            }


            // =================================================
            // COMMIT
            // =================================================

            conn.commit();


            mostrarExito(
                    reservationToEdit == null
                            ? "La reserva se creó correctamente.\n" +
                            "Número de reserva: " +
                            idReservation
                            : "La reserva se modificó correctamente.\n" +
                            "Número de reserva: " +
                            idReservation
            );


            limpiarFormulario();


        } catch (SQLException e) {

            try {

                if (conn != null) {

                    conn.rollback();
                }

            } catch (SQLException rollbackException) {

                System.err.println(
                        "Error al hacer rollback: "
                                + rollbackException.getMessage()
                );
            }


            System.err.println(
                    "Error SQL: "
                            + e.getMessage()
            );


            mostrarError(
                    "Ocurrió un error en la transacción."
            );


        } catch (Exception e) {

            try {

                if (conn != null) {

                    conn.rollback();
                }

            } catch (SQLException rollbackException) {

                System.err.println(
                        "Error al hacer rollback: "
                                + rollbackException.getMessage()
                );
            }


            System.err.println(
                    "Error: "
                            + e.getMessage()
            );


            e.printStackTrace();


            mostrarError(
                    "Ocurrió un error al guardar la reserva."
            );


        } finally {

            try {

                if (conn != null) {

                    conn.setAutoCommit(true);

                    conn.close();
                }

            } catch (SQLException e) {

                System.err.println(
                        "Error al cerrar la conexión: "
                                + e.getMessage()
                );
            }
        }
    }


    // =========================================================
    // VOLVER
    // =========================================================

    @FXML
    private void handleVolver() {

        if (dashboardController != null) {

            dashboardController.loadView(
                    "/views/Reservations.fxml"
            );

        } else {

            System.err.println(
                    "DashboardController no está conectado."
            );
        }
    }


    // =========================================================
    // CANCELAR
    // =========================================================

    @FXML
    private void handleCancel() {

        limpiarFormulario();
    }


    // =========================================================
    // LIMPIAR
    // =========================================================

    private void limpiarFormulario() {

        cmbCustomer.setValue(null);

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


    // =========================================================
    // ERROR
    // =========================================================

    private void mostrarError(
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle("Error");

        alert.setHeaderText(
                "No se pudo guardar"
        );

        alert.setContentText(
                mensaje
        );

        alert.showAndWait();
    }


    // =========================================================
    // ÉXITO
    // =========================================================

    private void mostrarExito(
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Operación exitosa"
        );

        alert.setHeaderText(
                "Operación realizada correctamente"
        );

        alert.setContentText(
                mensaje
        );

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