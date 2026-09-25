package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Customer;
import models.Reservation;
import models.ReservationStatus;
import repositories.CustomerDAO;
import repositories.ReservationRepo;
import repositories.ReservationStatusRepo;

import java.util.List;

public class ReservationsController {

    private static final int MAX_RESERVAS_VISIBLES = 100;

    private final ReservationRepo reservationRepo;
    private final CustomerDAO customerDAO;
    private final ReservationStatusRepo reservationStatusRepo;

    private DashboardController dashboardController;

    private final ObservableList<Reservation> todasLasReservas =
            FXCollections.observableArrayList();

    private List<Customer> customers;
    private List<ReservationStatus> reservationStatuses;

    @FXML private TableView<Reservation> tblReservations;
    @FXML private TableColumn<Reservation, Integer> colIdReservation;
    @FXML private TableColumn<Reservation, String> colCustomer;
    @FXML private TableColumn<Reservation, Object> colCheckIn;
    @FXML private TableColumn<Reservation, Object> colCheckOut;
    @FXML private TableColumn<Reservation, Integer> colGuests;
    @FXML private TableColumn<Reservation, Object> colTotalRate;
    @FXML private TableColumn<Reservation, String> colStatus;

    @FXML private TextField txtBuscarReserva;
    @FXML private ComboBox<String> cmbEstadoReserva;
    @FXML private Label lblTotalReservations;

    @FXML private Label lblDetailTitle;
    @FXML private TextField txtDetailReservationNumber;
    @FXML private TextField txtDetailCustomer;
    @FXML private TextField txtDetailCheckIn;
    @FXML private TextField txtDetailCheckOut;
    @FXML private TextField txtDetailGuests;
    @FXML private TextField txtDetailTotalRate;
    @FXML private TextField txtDetailStatus;
    @FXML private TextField txtDetailType;

    @FXML private Button btnViewMore;
    @FXML private Button btnEditReservation;
    @FXML private Button btnReservationConsumptions;

    public ReservationsController() {
        reservationRepo = new ReservationRepo();
        customerDAO = new CustomerDAO();
        reservationStatusRepo = new ReservationStatusRepo();
    }

    @FXML
    public void initialize() {
        loadCustomers();
        loadStatuses();
        configureTable();
        configureStatusFilter();
        configureSearch();
        loadReservations();
        configureSelection();
        applyFilters();
        clearDetail();

        btnViewMore.setDisable(true);
        btnEditReservation.setDisable(true);
        btnReservationConsumptions.setDisable(true);
    }

    private void configureTable() {

        colIdReservation.setCellValueFactory(
                new PropertyValueFactory<>("idReservation")
        );

        colCustomer.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            String customerName =
                    getCustomerName(reservation.getIdCustomer());

            return new SimpleStringProperty(customerName);
        });

        colCheckIn.setCellValueFactory(
                new PropertyValueFactory<>("checkIn")
        );

        colCheckOut.setCellValueFactory(
                new PropertyValueFactory<>("checkOut")
        );

        colGuests.setCellValueFactory(
                new PropertyValueFactory<>("numberOfGuests")
        );

        colTotalRate.setCellValueFactory(
                new PropertyValueFactory<>("totalRate")
        );

        colStatus.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            String statusName =
                    getStatusName(reservation.getIdReservationStatus());

            return new SimpleStringProperty(statusName);
        });

        colStatus.setCellFactory(column ->
                new TableCell<Reservation, String>() {

                    @Override
                    protected void updateItem(
                            String status,
                            boolean empty
                    ) {
                        super.updateItem(status, empty);

                        if (empty || status == null) {
                            setText(null);
                            setStyle("");
                            return;
                        }

                        setText(status);

                        switch (status.toLowerCase()) {

                            case "pendiente":
                                setStyle(
                                        "-fx-background-color: #fdfbea;" +
                                                "-fx-text-fill: #c0a72b;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-alignment: CENTER;"
                                );
                                break;

                            case "confirmada":
                                setStyle(
                                        "-fx-background-color: #e0ffcc;" +
                                                "-fx-text-fill: #1a6d03;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-alignment: CENTER;"
                                );
                                break;

                            case "cancelada":
                                setStyle(
                                        "-fx-background-color: #ffdde5;" +
                                                "-fx-text-fill: #8a1527;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-alignment: CENTER;"
                                );
                                break;

                            case "finalizada":
                                setStyle(
                                        "-fx-background-color: #e8e8e8;" +
                                                "-fx-text-fill: #666666;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-alignment: CENTER;"
                                );
                                break;

                            default:
                                setStyle("-fx-alignment: CENTER;");
                        }
                    }
                }
        );
    }

    private void configureSelection() {

        tblReservations.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldReservation, newReservation) -> {

                            if (newReservation != null) {

                                showDetail(newReservation);

                                btnEditReservation.setDisable(false);
                                btnReservationConsumptions.setDisable(false);
                                btnViewMore.setDisable(false);

                            } else {

                                clearDetail();

                                btnEditReservation.setDisable(true);
                                btnReservationConsumptions.setDisable(true);
                                btnViewMore.setDisable(true);
                            }
                        }
                );
    }

    private void showDetail(Reservation reservation) {

        if (reservation == null) {
            clearDetail();
            return;
        }

        String customerName =
                getCustomerName(reservation.getIdCustomer());

        String statusName =
                getStatusName(reservation.getIdReservationStatus());

        txtDetailReservationNumber.setText(
                String.valueOf(reservation.getIdReservation())
        );

        txtDetailCustomer.setText(
                getDisplayText(customerName)
        );

        txtDetailCheckIn.setText(
                getDisplayText(String.valueOf(reservation.getCheckIn()))
        );

        txtDetailCheckOut.setText(
                getDisplayText(String.valueOf(reservation.getCheckOut()))
        );

        txtDetailGuests.setText(
                String.valueOf(reservation.getNumberOfGuests())
        );

        txtDetailTotalRate.setText(
                getDisplayText(String.valueOf(reservation.getTotalRate()))
        );

        txtDetailStatus.setText(
                getDisplayText(statusName)
        );

        txtDetailType.setText(
                getDisplayText(
                        String.valueOf(reservation.getIdReservationType())
                )
        );

        lblDetailTitle.setText(
                "Reserva #" + reservation.getIdReservation()
        );
    }

    @FXML
    private void handleViewMore() {

        Reservation selectedReservation =
                tblReservations.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            return;
        }

        dashboardController.loadReservationDetail(selectedReservation);
    }

    private void clearDetail() {

        if (txtDetailReservationNumber != null)
            txtDetailReservationNumber.setText("--");

        if (txtDetailCustomer != null)
            txtDetailCustomer.setText("--");

        if (txtDetailCheckIn != null)
            txtDetailCheckIn.setText("--");

        if (txtDetailCheckOut != null)
            txtDetailCheckOut.setText("--");

        if (txtDetailGuests != null)
            txtDetailGuests.setText("--");

        if (txtDetailTotalRate != null)
            txtDetailTotalRate.setText("--");

        if (txtDetailStatus != null)
            txtDetailStatus.setText("--");

        if (txtDetailType != null)
            txtDetailType.setText("--");

        if (lblDetailTitle != null)
            lblDetailTitle.setText("Detalle de reserva");
    }

    private String getDisplayText(String value) {

        if (value == null || value.isBlank()) {
            return "--";
        }

        return value;
    }

    private void loadCustomers() {

        try {

            customers = customerDAO.listAll();

            System.out.println(
                    "Customers loaded: " + customers.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error loading customers: " + e.getMessage()
            );

            customers = List.of();
        }
    }

    private void loadStatuses() {

        try {

            reservationStatuses =
                    reservationStatusRepo.getReservationStatuses();

            System.out.println(
                    "Reservation statuses loaded: "
                            + reservationStatuses.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error loading reservation statuses: "
                            + e.getMessage()
            );

            reservationStatuses = List.of();
        }
    }

    private String getCustomerName(int idCustomer) {

        for (Customer customer : customers) {

            if (customer.getIdCustomer() == idCustomer) {

                return customer.getName()
                        + " "
                        + customer.getSurname();
            }
        }

        return "Unknown customer";
    }

    private String getStatusName(int idReservationStatus) {

        for (ReservationStatus status : reservationStatuses) {

            if (status.getIdReservationStatus()
                    == idReservationStatus) {

                return status.getName();
            }
        }

        return "Unknown";
    }

    private void loadReservations() {

        try {

            List<Reservation> reservations =
                    reservationRepo.getReservations();

            todasLasReservas.setAll(reservations);

            System.out.println(
                    "Reservations loaded: "
                            + reservations.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error loading reservations: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private void configureStatusFilter() {

        cmbEstadoReserva.setItems(
                FXCollections.observableArrayList(
                        "Activas",
                        "Todas",
                        "Pendiente",
                        "Confirmada",
                        "Finalizada",
                        "Cancelada"
                )
        );

        cmbEstadoReserva.setValue("Activas");

        cmbEstadoReserva.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        applyFilters()
        );
    }

    private void configureSearch() {

        txtBuscarReserva.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        applyFilters()
        );
    }

    private void applyFilters() {

        String text = txtBuscarReserva.getText();

        if (text == null) {
            text = "";
        }

        text = text.trim().toLowerCase();

        String selectedStatus =
                cmbEstadoReserva.getValue();

        ObservableList<Reservation> filtered =
                FXCollections.observableArrayList();

        /*
         * PRIMERO filtramos sobre TODAS las reservas.
         * Las que no aparecen en la grilla siguen estando
         * dentro de todasLasReservas.
         */
        for (Reservation reservation : todasLasReservas) {

            String id =
                    String.valueOf(
                            reservation.getIdReservation()
                    );

            String customer =
                    getCustomerName(
                            reservation.getIdCustomer()
                    ).toLowerCase();

            String status =
                    getStatusName(
                            reservation.getIdReservationStatus()
                    ).toLowerCase();

            boolean matchesText =
                    text.isEmpty()
                            || id.contains(text)
                            || customer.contains(text)
                            || status.contains(text);

            if (!matchesText) {
                continue;
            }

            boolean matchesStatus = true;

            if ("Activas".equals(selectedStatus)) {

                matchesStatus =
                        status.equals("pendiente")
                                || status.equals("confirmada");

            } else if ("Pendiente".equals(selectedStatus)) {

                matchesStatus =
                        status.equals("pendiente");

            } else if ("Confirmada".equals(selectedStatus)) {

                matchesStatus =
                        status.equals("confirmada");

            } else if ("Finalizada".equals(selectedStatus)) {

                matchesStatus =
                        status.equals("finalizada");

            } else if ("Cancelada".equals(selectedStatus)) {

                matchesStatus =
                        status.equals("cancelada");

            } else if ("Todas".equals(selectedStatus)) {

                matchesStatus = true;
            }

            if (matchesStatus) {
                filtered.add(reservation);
            }
        }

        /*
         * Si estamos en la vista normal y no estamos buscando,
         * mostramos solamente las primeras 100.
         *
         * Cuando el usuario busca algo o selecciona un estado
         * específico, mostramos todos los resultados encontrados.
         * Así las reservas que estaban ocultas pueden aparecer
         * mediante los filtros.
         */
        boolean hayBusqueda = !text.isEmpty();

        boolean estadoEspecifico =
                selectedStatus != null
                        && !"Activas".equals(selectedStatus)
                        && !"Todas".equals(selectedStatus);

        ObservableList<Reservation> visibles =
                FXCollections.observableArrayList();

        if (hayBusqueda || estadoEspecifico) {

            visibles.addAll(filtered);

        } else {

            int limite =
                    Math.min(
                            filtered.size(),
                            MAX_RESERVAS_VISIBLES
                    );

            for (int i = 0; i < limite; i++) {
                visibles.add(filtered.get(i));
            }
        }

        tblReservations.setItems(visibles);

        updateCounter(
                filtered.size(),
                visibles.size()
        );

        if (tblReservations.getSelectionModel()
                .getSelectedItem() == null) {

            clearDetail();

            btnEditReservation.setDisable(true);
            btnReservationConsumptions.setDisable(true);
            btnViewMore.setDisable(true);
        }
    }

    private void updateCounter(
            int totalEncontradas,
            int visibles
    ) {

        if (totalEncontradas > MAX_RESERVAS_VISIBLES
                && visibles == MAX_RESERVAS_VISIBLES) {

            lblTotalReservations.setText(
                    "Mostrando "
                            + visibles
                            + " de "
                            + totalEncontradas
                            + " reservas"
            );

        } else {

            lblTotalReservations.setText(
                    "Mostrando "
                            + visibles
                            + " reservas"
            );
        }
    }

    @FXML
    private void handleModifyReservation() {

        Reservation reservation =
                tblReservations.getSelectionModel()
                        .getSelectedItem();

        if (reservation == null) {
            return;
        }

        String statusName =
                getStatusName(
                        reservation.getIdReservationStatus()
                );

        if ("finalizada".equalsIgnoreCase(statusName)) {

            Alert alert =
                    new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Reserva finalizada");
            alert.setHeaderText(null);

            alert.setContentText(
                    "La reserva ya está finalizada "
                            + "y no puede ser modificada."
            );

            alert.showAndWait();

            return;
        }

        if (dashboardController != null) {

            dashboardController.loadEditReservation(
                    reservation
            );

        } else {

            System.err.println(
                    "DashboardController is not connected."
            );
        }
    }

    @FXML
    private void handleReservationConsumptions() {

        Reservation reservation =
                tblReservations.getSelectionModel()
                        .getSelectedItem();

        if (reservation == null) {
            return;
        }

        String statusName =
                getStatusName(
                        reservation.getIdReservationStatus()
                );

        if ("finalizada".equalsIgnoreCase(statusName)) {

            Alert alert =
                    new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Reserva finalizada");
            alert.setHeaderText(null);

            alert.setContentText(
                    "La reserva ya está finalizada "
                            + "y sus consumos no pueden modificarse."
            );

            alert.showAndWait();

            return;
        }

        if (dashboardController != null) {

            dashboardController.loadReservationConsumptions(
                    reservation
            );

        } else {

            System.err.println(
                    "DashboardController is not connected."
            );
        }
    }

    public void setDashboardController(
            DashboardController dashboardController
    ) {
        this.dashboardController = dashboardController;
    }

    @FXML
    private void handleNewReservation() {

        if (dashboardController != null) {

            dashboardController.loadView(
                    "/views/NewReservation.fxml"
            );

        } else {

            System.err.println(
                    "DashboardController is not connected."
            );
        }
    }

    public int createReservation(
            Reservation reservation
    ) {
        return reservationRepo.createReservation(
                reservation
        );
    }

    public List<Reservation> getReservations() {
        return reservationRepo.getReservations();
    }

    public Reservation getReservationById(
            int idReservation
    ) {
        return reservationRepo.getReservationById(
                idReservation
        );
    }

    public boolean updateReservation(
            Reservation reservation
    ) {
        return reservationRepo.updateReservation(
                reservation
        );
    }

    public boolean updateReservationStatus(
            int idReservation,
            int idReservationStatus
    ) {
        return reservationRepo.updateReservationStatus(
                idReservation,
                idReservationStatus
        );
    }
}