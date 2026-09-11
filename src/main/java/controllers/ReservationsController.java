package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Customer;
import models.Reservation;
import models.ReservationStatus;
import repositories.CustomerDAO;
import repositories.ReservationRepo;
import repositories.ReservationStatusRepo;

import java.util.List;

public class ReservationsController {

    private final ReservationRepo reservationRepo;
    private final CustomerDAO customerDAO;
    private final ReservationStatusRepo reservationStatusRepo;
    private DashboardController dashboardController;

    private final ObservableList<Reservation> todasLasReservas =
            FXCollections.observableArrayList();

    private List<Customer> customers;
    private List<ReservationStatus> reservationStatuses;

    @FXML
    private TableView<Reservation> tblReservations;

    @FXML
    private TableColumn<Reservation, Integer> colIdReservation;

    @FXML
    private TableColumn<Reservation, String> colCustomer;

    @FXML
    private TableColumn<Reservation, Object> colCheckIn;

    @FXML
    private TableColumn<Reservation, Object> colCheckOut;

    @FXML
    private TableColumn<Reservation, Integer> colGuests;

    @FXML
    private TableColumn<Reservation, Object> colTotalRate;

    @FXML
    private TableColumn<Reservation, String> colStatus;

    @FXML
    private TableColumn<Reservation, Void> colActions;

    @FXML
    private TextField txtBuscarReserva;

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
        loadReservations();
        configureSearch();
    }

    private void configureTable() {
        colIdReservation.setCellValueFactory(
                new PropertyValueFactory<>("idReservation")
        );

        colCustomer.setCellValueFactory(cellData -> {
            Reservation reservation = cellData.getValue();
            String customerName = getCustomerName(reservation.getIdCustomer());
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
            String statusName = getStatusName(
                    reservation.getIdReservationStatus()
            );
            return new SimpleStringProperty(statusName);
        });

        colStatus.setCellFactory(column ->
                new TableCell<Reservation, String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
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

                            case "pagado":
                                setStyle(
                                        "-fx-background-color: #fff4cc;" +
                                                "-fx-text-fill: #30b80b;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-alignment: CENTER;"
                                );
                                break;

                            case "cancelado":
                                setStyle(
                                        "-fx-background-color: #e3f3e7;" +
                                                "-fx-text-fill: #7d2e3a;" +
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

        configureActionsColumn();
    }

    private void loadCustomers() {
        try {
            customers = customerDAO.listAll();
            System.out.println("Customers loaded: " + customers.size());
        } catch (Exception e) {
            System.err.println("Error loading customers: " + e.getMessage());
            customers = List.of();
        }
    }

    private void loadStatuses() {
        try {
            reservationStatuses =
                    reservationStatusRepo.getReservationStatuses();

            System.out.println(
                    "Reservation statuses loaded: " +
                            reservationStatuses.size()
            );
        } catch (Exception e) {
            System.err.println(
                    "Error loading reservation statuses: " +
                            e.getMessage()
            );
            reservationStatuses = List.of();
        }
    }

    private String getCustomerName(int idCustomer) {
        for (Customer customer : customers) {
            if (customer.getIdCustomer() == idCustomer) {
                return customer.getName() + " " + customer.getSurname();
            }
        }

        return "Unknown customer";
    }

    private String getStatusName(int idReservationStatus) {
        for (ReservationStatus status : reservationStatuses) {
            if (status.getIdReservationStatus() == idReservationStatus) {
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
            tblReservations.setItems(
                    FXCollections.observableArrayList(reservations)
            );

            System.out.println(
                    "Reservations loaded: " + reservations.size()
            );
        } catch (Exception e) {
            System.err.println(
                    "Error loading reservations: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void configureSearch() {
        txtBuscarReserva.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    String text = newValue.trim().toLowerCase();

                    if (text.isEmpty()) {
                        tblReservations.setItems(
                                FXCollections.observableArrayList(
                                        todasLasReservas
                                )
                        );
                        return;
                    }

                    ObservableList<Reservation> filtered =
                            FXCollections.observableArrayList();

                    for (Reservation reservation : todasLasReservas) {
                        String id = String.valueOf(
                                reservation.getIdReservation()
                        );

                        String customer = getCustomerName(
                                reservation.getIdCustomer()
                        ).toLowerCase();

                        String status = getStatusName(
                                reservation.getIdReservationStatus()
                        ).toLowerCase();

                        if (id.contains(text)
                                || customer.contains(text)
                                || status.contains(text)) {
                            filtered.add(reservation);
                        }
                    }

                    tblReservations.setItems(filtered);
                }
        );
    }

    private void configureActionsColumn() {
        colActions.setCellFactory(param ->
                new TableCell<Reservation, Void>() {

                    private final Button btnModify =
                            new Button("Modificar");

                    private final HBox buttons = new HBox(8);

                    {
                        buttons.getChildren().add(btnModify);
                        buttons.setAlignment(Pos.CENTER);

                        btnModify.setOnAction(event -> {
                            Reservation reservation =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            handleModify(reservation);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        setGraphic(empty ? null : buttons);
                    }
                }
        );
    }

    private void handleModify(Reservation reservation) {
        if (reservation == null) {
            return;
        }

        if (dashboardController != null) {
            dashboardController.loadEditReservation(reservation);
        } else {
            System.err.println(
                    "DashboardController is not connected."
            );
        }
    }

    public void setDashboardController(
            DashboardController dashboardController) {

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

    public int createReservation(Reservation reservation) {
        return reservationRepo.createReservation(reservation);
    }

    public List<Reservation> getReservations() {
        return reservationRepo.getReservations();
    }

    public Reservation getReservationById(int idReservation) {
        return reservationRepo.getReservationById(idReservation);
    }

    public boolean updateReservation(Reservation reservation) {
        return reservationRepo.updateReservation(reservation);
    }

    public boolean updateReservationStatus(
            int idReservation,
            int idReservationStatus) {

        return reservationRepo.updateReservationStatus(
                idReservation,
                idReservationStatus
        );
    }
}
