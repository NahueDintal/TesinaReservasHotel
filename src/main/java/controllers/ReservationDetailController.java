package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.Consumption;
import models.Payment;
import models.Product;
import models.Reservation;
import models.Service;
import repositories.ConsumptionRepo;
import repositories.PaymentRepo;
import repositories.ProductRepo;
import repositories.ServiceRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationDetailController {

    // =========================================================
    // RESERVA
    // =========================================================

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblCustomer;

    @FXML
    private Label lblCheckIn;

    @FXML
    private Label lblCheckOut;

    @FXML
    private Label lblNights;

    @FXML
    private Label lblGuests;

    @FXML
    private Label lblRoom;

    @FXML
    private Label lblReservationType;

    @FXML
    private Label lblOrigin;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblObservations;


    // =========================================================
    // PAGOS
    // =========================================================

    @FXML
    private TableView<Payment> tblPayments;

    @FXML
    private TableColumn<Payment, String> colPaymentDate;

    @FXML
    private TableColumn<Payment, String> colPaymentAmount;

    @FXML
    private TableColumn<Payment, String> colPaymentMethod;

    @FXML
    private TableColumn<Payment, String> colPaymentStatus;

    @FXML
    private TableColumn<Payment, String> colPaymentObservations;

    @FXML
    private Label lblTotalPaid;


    // =========================================================
    // CONSUMOS
    // =========================================================

    @FXML
    private TableView<Consumption> tblConsumptions;

    @FXML
    private TableColumn<Consumption, String> colConsumptionType;

    @FXML
    private TableColumn<Consumption, String> colConsumptionName;

    @FXML
    private TableColumn<Consumption, String> colConsumptionQuantity;

    @FXML
    private TableColumn<Consumption, String> colConsumptionUnitPrice;

    @FXML
    private TableColumn<Consumption, String> colConsumptionTotal;

    @FXML
    private TableColumn<Consumption, String> colConsumptionDate;

    @FXML
    private Label lblTotalConsumptions;


    // =========================================================
    // BOTÓN
    // =========================================================

    @FXML
    private Button btnClose;


    // =========================================================
    // REPOSITORIOS
    // =========================================================

    private final PaymentRepo paymentRepo = new PaymentRepo();
    private final ConsumptionRepo consumptionRepo = new ConsumptionRepo();
    private final ProductRepo productRepo = new ProductRepo();
    private final ServiceRepo serviceRepo = new ServiceRepo();


    // =========================================================
    // DATOS AUXILIARES
    // =========================================================

    private Reservation reservation;

    private DashboardController dashboardController;

    private final Map<Integer, String> productNames =
            new HashMap<>();

    private final Map<Integer, String> serviceNames =
            new HashMap<>();


    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    // =========================================================
    // DASHBOARD
    // =========================================================

    public void setDashboardController(
            DashboardController dashboardController) {

        this.dashboardController = dashboardController;
    }


    // =========================================================
    // RESERVA
    // =========================================================

    public void setReservation(Reservation reservation) {

        this.reservation = reservation;

        loadCatalogNames();
        loadReservationData();
        loadPayments();
        loadConsumptions();
    }


    // =========================================================
    // CARGAR NOMBRES DE PRODUCTOS Y SERVICIOS
    // =========================================================

    private void loadCatalogNames() {

        productNames.clear();
        serviceNames.clear();

        List<Product> activeProducts =
                productRepo.getActiveProducts();

        List<Product> inactiveProducts =
                productRepo.getInactiveProducts();

        for (Product product : activeProducts) {

            productNames.put(
                    product.getIdProduct(),
                    product.getName()
            );
        }

        for (Product product : inactiveProducts) {

            productNames.put(
                    product.getIdProduct(),
                    product.getName()
            );
        }


        List<Service> activeServices =
                serviceRepo.getActiveServices();

        List<Service> inactiveServices =
                serviceRepo.getInactiveServices();

        for (Service service : activeServices) {

            serviceNames.put(
                    service.getIdService(),
                    service.getName()
            );
        }

        for (Service service : inactiveServices) {

            serviceNames.put(
                    service.getIdService(),
                    service.getName()
            );
        }
    }


    // =========================================================
    // INFORMACIÓN DE RESERVA
    // =========================================================

    private void loadReservationData() {

        if (reservation == null) {
            return;
        }

        lblTitle.setText(
                "Detalle de reserva #" +
                        reservation.getIdReservation()
        );

        lblCustomer.setText(
                "Cliente #" +
                        reservation.getIdCustomer()
        );

        lblCheckIn.setText(
                String.valueOf(
                        reservation.getCheckIn()
                )
        );

        lblCheckOut.setText(
                String.valueOf(
                        reservation.getCheckOut()
                )
        );

        // Calcular noches
        if (reservation.getCheckIn() != null &&
                reservation.getCheckOut() != null) {

            long nights =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            reservation.getCheckIn(),
                            reservation.getCheckOut()
                    );

            lblNights.setText(
                    String.valueOf(nights)
            );

        } else {

            lblNights.setText("-");
        }

        lblGuests.setText(
                String.valueOf(
                        reservation.getNumberOfGuests()
                )
        );

        // Todavía no existe idRoom en Reservation
        lblRoom.setText("-");

        lblReservationType.setText(
                String.valueOf(
                        reservation.getIdReservationType()
                )
        );

        lblOrigin.setText("-");

        lblStatus.setText(
                String.valueOf(
                        reservation.getIdReservationStatus()
                )
        );

        String observations =
                reservation.getObservations();

        if (observations == null ||
                observations.isBlank()) {

            lblObservations.setText(
                    "Sin observaciones"
            );

        } else {

            lblObservations.setText(
                    observations
            );
        }
    }


    // =========================================================
    // PAGOS
    // =========================================================

    private void loadPayments() {

        if (reservation == null) {
            return;
        }

        List<Payment> payments =
                paymentRepo.getPaymentsByReservation(
                        reservation.getIdReservation()
                );

        configurePaymentColumns();

        tblPayments.setItems(
                FXCollections.observableArrayList(
                        payments
                )
        );

        BigDecimal totalPaid =
                BigDecimal.ZERO;

        for (Payment payment : payments) {

            if (payment.getAmount() != null) {

                totalPaid =
                        totalPaid.add(
                                payment.getAmount()
                        );
            }
        }

        lblTotalPaid.setText(
                formatMoney(totalPaid)
        );
    }


    private void configurePaymentColumns() {

        colPaymentDate.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        formatDateTime(
                                cell.getValue()
                                        .getPaymentDate()
                        )
                )
        );

        colPaymentAmount.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        formatMoney(
                                cell.getValue()
                                        .getAmount()
                        )
                )
        );

        colPaymentMethod.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        "ID " +
                                cell.getValue()
                                        .getIdPaymentMethod()
                )
        );

        colPaymentStatus.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        "ID " +
                                cell.getValue()
                                        .getIdPaymentStatus()
                )
        );

        colPaymentObservations.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        safeText(
                                cell.getValue()
                                        .getObservations()
                        )
                )
        );
    }


    // =========================================================
    // CONSUMOS
    // =========================================================

    private void loadConsumptions() {

        if (reservation == null) {
            return;
        }

        try {

            List<Consumption> consumptions =
                    consumptionRepo.getConsumptionsByReservation(
                            reservation.getIdReservation()
                    );

            configureConsumptionColumns();

            tblConsumptions.setItems(
                    FXCollections.observableArrayList(
                            consumptions
                    )
            );

            BigDecimal total =
                    BigDecimal.ZERO;

            for (Consumption consumption : consumptions) {

                if (consumption.getTotal() != null) {

                    total = total.add(
                            consumption.getTotal()
                    );
                }
            }

            lblTotalConsumptions.setText(
                    formatMoney(total)
            );

        } catch (java.sql.SQLException e) {

            System.err.println(
                    "Error al cargar los consumos de la reserva: "
                            + e.getMessage()
            );

            tblConsumptions.setItems(
                    FXCollections.observableArrayList()
            );

            lblTotalConsumptions.setText(
                    "$0.00"
            );
        }
    }

    private void configureConsumptionColumns() {

        colConsumptionType.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        getConsumptionTypeName(
                                cell.getValue()
                                        .getIdConsumptionType()
                        )
                )
        );

        colConsumptionName.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        getConsumptionName(
                                cell.getValue()
                        )
                )
        );

        colConsumptionQuantity.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        String.valueOf(
                                cell.getValue()
                                        .getQuantity()
                        )
                )
        );

        colConsumptionUnitPrice.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        formatMoney(
                                cell.getValue()
                                        .getUnitPrice()
                        )
                )
        );

        colConsumptionTotal.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        formatMoney(
                                cell.getValue()
                                        .getTotal()
                        )
                )
        );

        colConsumptionDate.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        formatDateTime(
                                cell.getValue()
                                        .getConsumptionDate()
                        )
                )
        );
    }


    private String getConsumptionTypeName(
            int idConsumptionType) {

        if (idConsumptionType == 1) {
            return "Producto";
        }

        if (idConsumptionType == 2) {
            return "Servicio";
        }

        return "Desconocido";
    }


    private String getConsumptionName(
            Consumption consumption) {

        if (consumption.getIdConsumptionType() == 1) {

            return productNames.getOrDefault(
                    consumption.getIdProduct(),
                    "Producto #" +
                            consumption.getIdProduct()
            );
        }

        if (consumption.getIdConsumptionType() == 2) {

            return serviceNames.getOrDefault(
                    consumption.getIdService(),
                    "Servicio #" +
                            consumption.getIdService()
            );
        }

        return "-";
    }


    // =========================================================
    // CERRAR
    // =========================================================

    @FXML
    private void handleClose() {

        if (dashboardController != null) {

            dashboardController.loadView(
                    "/views/reservations.fxml"
            );
        }
    }


    // =========================================================
    // FORMATOS
    // =========================================================

    private String formatMoney(BigDecimal amount) {

        if (amount == null) {
            return "$0.00";
        }

        return "$" + amount.toPlainString();
    }


    private String formatDateTime(
            LocalDateTime dateTime) {

        if (dateTime == null) {
            return "-";
        }

        return dateTimeFormatter.format(
                dateTime
        );
    }


    private String safeText(String text) {

        if (text == null ||
                text.isBlank()) {

            return "-";
        }

        return text;
    }
}

