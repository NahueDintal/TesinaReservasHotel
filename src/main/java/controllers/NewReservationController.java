package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    private ComboBox<Integer> cmbCustomer;

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

    // REEMPLAZA AL LABEL QUE DABA ERROR
    @FXML
    private TextField txtConsumptionTotal;


    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final ReservationRepo reservationRepo;
    private final ReservationStatusRepo reservationStatusRepo;
    private final ReservationTypeRepo reservationTypeRepo;

    private final PaymentRepo paymentRepo;
    private final PaymentMethodRepo paymentMethodRepo;
    private final PaymentStatusRepo paymentStatusRepo;

    private final ConsumptionRepo consumptionRepo;

    private final ProductRepo productRepo;
    private final ServiceRepo serviceRepo;


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
        reservationStatusRepo = new ReservationStatusRepo();
        reservationTypeRepo = new ReservationTypeRepo();

        paymentRepo = new PaymentRepo();
        paymentMethodRepo = new PaymentMethodRepo();
        paymentStatusRepo = new PaymentStatusRepo();

        consumptionRepo = new ConsumptionRepo();

        productRepo = new ProductRepo();
        serviceRepo = new ServiceRepo();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        System.out.println(
                "NewReservationController iniciado"
        );

        configureConsumptionTable();

        // Tipo de consumo:
        // 1 = Producto
        // 2 = Servicio
        cmbConsumptionType.setItems(
                FXCollections.observableArrayList(1, 2)
        );

        // Cargamos los datos de los ComboBox
        loadReservationStatuses();
        loadReservationTypes();

        loadPaymentMethods();
        loadPaymentStatuses();

        loadProducts();
        loadServices();

        // El total comienza en 0
        txtConsumptionTotal.setText("0.00");
        txtConsumptionTotal.setEditable(false);
    }


    // =========================================================
    // RESERVATION STATUS
    // =========================================================

    private void loadReservationStatuses() {

        try {

            List<ReservationStatus> statuses =
                    reservationStatusRepo.getReservationStatuses();

            cmbReservationStatus.getItems().clear();
            cmbReservationStatus.getItems().addAll(statuses);

        } catch (Exception e) {

            System.err.println(
                    "Error cargando estados de reserva: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // RESERVATION TYPE
    // =========================================================

    private void loadReservationTypes() {

        try {

            List<ReservationType> types =
                    reservationTypeRepo.getReservationTypes();

            cmbReservationType.getItems().clear();
            cmbReservationType.getItems().addAll(types);

        } catch (Exception e) {

            System.err.println(
                    "Error cargando tipos de reserva: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // PAYMENT METHOD
    // =========================================================

    private void loadPaymentMethods() {

        try {

            List<PaymentMethod> methods =
                    paymentMethodRepo.getPaymentMethods();

            cmbPaymentMethod.getItems().clear();
            cmbPaymentMethod.getItems().addAll(methods);

        } catch (Exception e) {

            System.err.println(
                    "Error cargando métodos de pago: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // PAYMENT STATUS
    // =========================================================

    private void loadPaymentStatuses() {

        try {

            List<PaymentStatus> statuses =
                    paymentStatusRepo.getPaymentStatuses();

            cmbPaymentStatus.getItems().clear();
            cmbPaymentStatus.getItems().addAll(statuses);

        } catch (Exception e) {

            System.err.println(
                    "Error cargando estados de pago: "
                            + e.getMessage()
            );
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
            cmbProduct.getItems().addAll(products);

        } catch (Exception e) {

            System.err.println(
                    "Error cargando productos: "
                            + e.getMessage()
            );
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
            cmbService.getItems().addAll(services);

        } catch (Exception e) {

            System.err.println(
                    "Error cargando servicios: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CONFIGURAR TABLA DE CONSUMOS
    // =========================================================

    private void configureConsumptionTable() {

        colConsumptionQuantity.setCellValueFactory(
                cellData ->
                        new javafx.beans.property.SimpleIntegerProperty(
                                cellData.getValue().getQuantity()
                        ).asObject()
        );

        colConsumptionUnitPrice.setCellValueFactory(
                cellData ->
                        new javafx.beans.property.SimpleObjectProperty<>(
                                cellData.getValue().getUnitPrice()
                        )
        );

        colConsumptionTotal.setCellValueFactory(
                cellData ->
                        new javafx.beans.property.SimpleObjectProperty<>(
                                cellData.getValue().getTotal()
                        )
        );

        tblConsumptions.setItems(consumptions);
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
                txtConsumptionQuantity.getText().trim();

        if (textoQuantity.isEmpty()) {

            mostrarError(
                    "Debe ingresar la cantidad."
            );

            return;
        }


        int quantity;

        try {

            quantity =
                    Integer.parseInt(textoQuantity);

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

        } else if (consumptionType == 2) {

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

        } else {

            mostrarError(
                    "El tipo de consumo seleccionado no es válido."
            );

            return;
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


        consumptions.add(consumption);

        updateConsumptionTotal();


        cmbProduct.setValue(null);
        cmbService.setValue(null);

        txtConsumptionQuantity.clear();
    }


    // =========================================================
    // ACTUALIZAR TOTAL CONSUMOS
    // =========================================================

    private void updateConsumptionTotal() {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Consumption consumption : consumptions) {

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
    // GUARDAR RESERVA
    // =========================================================

    @FXML
    private void handleSave() {

        Connection conn = null;

        try {

            // =================================================
            // CLIENTE
            // =================================================

            Integer idCustomer =
                    cmbCustomer.getValue();

            if (idCustomer == null || idCustomer <= 0) {

                mostrarError(
                        "Debe seleccionar un cliente."
                );

                return;
            }


            // =================================================
            // FECHAS
            // =================================================

            LocalDate checkIn =
                    dpCheckIn.getValue();

            if (checkIn == null) {

                mostrarError(
                        "Debe seleccionar la fecha de check-in."
                );

                return;
            }


            LocalDate checkOut =
                    dpCheckOut.getValue();

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
                    txtNumberOfGuests.getText().trim();

            if (textoGuests.isEmpty()) {

                mostrarError(
                        "Debe ingresar la cantidad de huéspedes."
                );

                return;
            }


            int numberOfGuests;

            try {

                numberOfGuests =
                        Integer.parseInt(textoGuests);

            } catch (NumberFormatException e) {

                mostrarError(
                        "La cantidad de huéspedes debe contener " +
                                "solamente números."
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
                    txtTotalRate.getText().trim();

            if (textoRate.isEmpty()) {

                mostrarError(
                        "Debe ingresar la tarifa total."
                );

                return;
            }


            BigDecimal totalRate;

            try {

                totalRate =
                        new BigDecimal(textoRate);

            } catch (NumberFormatException e) {

                mostrarError(
                        "La tarifa debe contener solamente números."
                );

                return;
            }


            if (totalRate.compareTo(BigDecimal.ZERO) < 0) {

                mostrarError(
                        "La tarifa total no puede ser negativa."
                );

                return;
            }


            // =================================================
            // ESTADO RESERVA
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
            // TIPO RESERVA
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
                    txtReservationObservations.getText();

            if (reservationObservations != null &&
                    reservationObservations.trim().isEmpty()) {

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
                            reservationStatus.getIdReservationStatus(),
                            reservationType.getIdReservationType(),
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
            // CREAR RESERVA
            // =================================================

            int idReservation =
                    reservationRepo.createReservation(
                            conn,
                            reservation
                    );


            if (idReservation <= 0) {

                conn.rollback();

                mostrarError(
                        "No se pudo crear la reserva."
                );

                return;
            }

            // =========================
            // CONSUMPTIONS
            // =========================

            for (Consumption consumption : consumptions) {

                consumption.setIdReservation(idReservation);

                boolean consumptionCreated =
                        consumptionRepo.createConsumption(
                                conn,
                                consumption
                        );

                if (!consumptionCreated) {

                    conn.rollback();

                    mostrarError(
                            "No se pudo registrar uno de los consumos. " +
                                    "La reserva tampoco fue guardada."
                    );

                    return;
                }
            }

            // =================================================
            // PAYMENT
            // =================================================

            String textoPayment =
                    txtPaymentAmount.getText().trim();


            // El pago es opcional
            if (!textoPayment.isEmpty()) {

                BigDecimal paymentAmount;

                try {

                    paymentAmount =
                            new BigDecimal(textoPayment);

                } catch (NumberFormatException e) {

                    conn.rollback();

                    mostrarError(
                            "El importe del pago debe contener " +
                                    "solamente números."
                    );

                    return;
                }


                if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {

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
                        dpPaymentDate.getValue()
                                .atStartOfDay();


                String paymentObservations =
                        txtPaymentObservations.getText();

                if (paymentObservations != null &&
                        paymentObservations.trim().isEmpty()) {

                    paymentObservations = null;
                }


                Payment payment =
                        new Payment(
                                idReservation,
                                paymentAmount,
                                paymentDate,
                                paymentMethod.getIdPaymentMethod(),
                                paymentStatus.getIdPaymentStatus(),
                                paymentObservations
                        );


                boolean paymentCreated =
                        paymentRepo.createPayment(
                                conn,
                                payment
                        );


                if (!paymentCreated) {

                    conn.rollback();

                    mostrarError(
                            "No se pudo registrar el pago. " +
                                    "La reserva tampoco fue guardada."
                    );

                    return;
                }
            }


            // =================================================
            // COMMIT
            // =================================================

            conn.commit();


            mostrarExito(
                    "La reserva se creó correctamente.\n" +
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
                        "Error al hacer rollback: " +
                                rollbackException.getMessage()
                );
            }


            mostrarError(
                    "Ocurrió un error en la transacción."
            );


            System.err.println(
                    "Error SQL: " +
                            e.getMessage()
            );


        } catch (Exception e) {

            try {

                if (conn != null) {
                    conn.rollback();
                }

            } catch (SQLException rollbackException) {

                System.err.println(
                        "Error al hacer rollback: " +
                                rollbackException.getMessage()
                );
            }


            mostrarError(
                    "Ocurrió un error al guardar la reserva."
            );


            System.err.println(
                    "Error: " +
                            e.getMessage()
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


        // CONSUMPTION

        // Limpiar consumos
        consumptions.clear();

        if (tblConsumptions != null) {
            tblConsumptions.refresh();
        }

        updateConsumptionTotal();

        cmbConsumptionType.setValue(null);

        cmbProduct.setValue(null);
        cmbService.setValue(null);

        txtConsumptionQuantity.clear();

        consumptions.clear();

        txtConsumptionTotal.setText("0.00");
    }


    // =========================================================
    // ERROR
    // =========================================================

    private void mostrarError(String mensaje) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText("No se pudo guardar");

        alert.setContentText(mensaje);

        alert.showAndWait();
    }


    // =========================================================
    // ÉXITO
    // =========================================================

    private void mostrarExito(String mensaje) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Reserva creada");
        alert.setHeaderText("Operación exitosa");

        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}