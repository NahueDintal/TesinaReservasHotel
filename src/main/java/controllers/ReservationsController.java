package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import models.Reservation;
import repositories.ReservationRepo;

import java.util.List;

public class ReservationsController {

    private final ReservationRepo reservationRepo;

    private DashboardController dashboardController;

// =========================================================
// TABLA
// =========================================================

    @FXML
    private TableView<Reservation> tblReservations;

    @FXML
    private TableColumn<Reservation, Integer> colIdReservation;

    @FXML
    private TableColumn<Reservation, Integer> colCustomer;

    @FXML
    private TableColumn<Reservation, Object> colCheckIn;

    @FXML
    private TableColumn<Reservation, Object> colCheckOut;

    @FXML
    private TableColumn<Reservation, Integer> colGuests;

    @FXML
    private TableColumn<Reservation, Object> colTotalRate;

    @FXML
    private TableColumn<Reservation, Integer> colStatus;


// =========================================================
// CONSTRUCTOR
// =========================================================

    public ReservationsController() {
        reservationRepo = new ReservationRepo();
    }


// =========================================================
// INITIALIZE
// =========================================================

    @FXML
    public void initialize() {

        configurarTabla();
        cargarReservas();
    }


// =========================================================
// CONFIGURAR TABLA
// =========================================================

    private void configurarTabla() {

        colIdReservation.setCellValueFactory(
                new PropertyValueFactory<>("idReservation")
        );

        colCustomer.setCellValueFactory(
                new PropertyValueFactory<>("idCustomer")
        );

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

        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("idReservationStatus")
        );
    }


// =========================================================
// CARGAR RESERVAS
// =========================================================

    private void cargarReservas() {

        try {

            List<Reservation> reservations =
                    reservationRepo.getReservations();

            tblReservations.setItems(
                    FXCollections.observableArrayList(
                            reservations
                    )
            );

            System.out.println(
                    "Reservas cargadas: " +
                            reservations.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error cargando reservas: " +
                            e.getMessage()
            );

            e.printStackTrace();
        }
    }


// =========================================================
// CONECTAR CON DASHBOARD
// =========================================================

    public void setDashboardController(
            DashboardController dashboardController) {

        this.dashboardController = dashboardController;
    }


// =========================================================
// NUEVA RESERVA
// =========================================================

    @FXML
    private void handleNuevaReserva() {

        if (dashboardController != null) {

            dashboardController.loadView(
                    "/views/NewReservation.fxml"
            );

        } else {

            System.err.println(
                    "DashboardController no está conectado."
            );
        }
    }


// =========================================================
// CREAR RESERVA
// =========================================================

    public int createReservation(
            Reservation reservation) {

        return reservationRepo.createReservation(
                reservation
        );
    }


// =========================================================
// OBTENER TODAS LAS RESERVAS
// =========================================================

    public List<Reservation> getReservations() {

        return reservationRepo.getReservations();
    }


// =========================================================
// OBTENER RESERVA POR ID
// =========================================================

    public Reservation getReservationById(
            int idReservation) {

        return reservationRepo.getReservationById(
                idReservation
        );
    }


// =========================================================
// ACTUALIZAR RESERVA
// =========================================================

    public boolean updateReservation(
            Reservation reservation) {

        return reservationRepo.updateReservation(
                reservation
        );
    }


// =========================================================
// ACTUALIZAR ESTADO
// =========================================================

    public boolean updateReservationStatus(
            int idReservation,
            int idReservationStatus) {

        return reservationRepo.updateReservationStatus(
                idReservation,
                idReservationStatus
        );
    }

}
