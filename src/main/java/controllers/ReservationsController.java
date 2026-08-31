package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.TableCell;

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

    // =========================================================
    // DATOS
    // =========================================================

    private final ObservableList<Reservation> todasLasReservas =
            FXCollections.observableArrayList();

    private List<Customer> customers;
    private List<ReservationStatus> reservationStatuses;


    // =========================================================
    // TABLA
    // =========================================================

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


    // =========================================================
    // BÚSQUEDA
    // =========================================================

    @FXML
    private TextField txtBuscarReserva;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ReservationsController() {

        reservationRepo = new ReservationRepo();
        customerDAO = new CustomerDAO();
        reservationStatusRepo = new ReservationStatusRepo();
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    @FXML
    public void initialize() {

        cargarClientes();
        cargarEstados();

        configurarTabla();
        cargarReservas();

        configurarBusqueda();
    }


    // =========================================================
    // CONFIGURAR TABLA
    // =========================================================

    private void configurarTabla() {

        // Número de reserva
        colIdReservation.setCellValueFactory(
                new PropertyValueFactory<>("idReservation")
        );


        // =====================================================
        // CLIENTE
        // =====================================================

        colCustomer.setCellValueFactory(cellData -> {

            Reservation reservation =
                    cellData.getValue();

            String nombreCliente =
                    obtenerNombreCliente(
                            reservation.getIdCustomer()
                    );

            return new SimpleStringProperty(nombreCliente);
        });


        // =====================================================
        // FECHAS
        // =====================================================

        colCheckIn.setCellValueFactory(
                new PropertyValueFactory<>("checkIn")
        );

        colCheckOut.setCellValueFactory(
                new PropertyValueFactory<>("checkOut")
        );


        // =====================================================
        // HUÉSPEDES
        // =====================================================

        colGuests.setCellValueFactory(
                new PropertyValueFactory<>("numberOfGuests")
        );


        // =====================================================
        // TARIFA
        // =====================================================

        colTotalRate.setCellValueFactory(
                new PropertyValueFactory<>("totalRate")
        );


        // =====================================================
        // ESTADO
        // =====================================================

        colStatus.setCellValueFactory(cellData -> {

            Reservation reservation =
                    cellData.getValue();

            String nombreEstado =
                    obtenerNombreEstado(
                            reservation.getIdReservationStatus()
                    );

            return new SimpleStringProperty(nombreEstado);
        });

        colStatus.setCellFactory(column -> new TableCell<Reservation, String>() {

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
                                "-fx-background-color: #fdeaea;" +
                                        "-fx-text-fill: #c0392b;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-alignment: CENTER;"
                        );
                        break;

                    case "pagado":
                        setStyle(
                                "-fx-background-color: #fff4cc;" +
                                        "-fx-text-fill: #b8860b;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-alignment: CENTER;"
                        );
                        break;

                    case "cancelado":
                        setStyle(
                                "-fx-background-color: #e3f3e7;" +
                                        "-fx-text-fill: #2e7d45;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-alignment: CENTER;"
                        );
                        break;

                    default:
                        setStyle(
                                "-fx-alignment: CENTER;"
                        );
                        break;
                }
            }
        });


        // =====================================================
        // ACCIONES
        // =====================================================

        configurarColumnaAcciones();
    }


    // =========================================================
    // CLIENTES
    // =========================================================

    private void cargarClientes() {

        try {

            customers =
                    customerDAO.listAll();

            System.out.println(
                    "Clientes cargados: " +
                            customers.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error cargando clientes: " +
                            e.getMessage()
            );

            customers = List.of();
        }
    }


    // =========================================================
    // ESTADOS
    // =========================================================

    private void cargarEstados() {

        try {

            reservationStatuses =
                    reservationStatusRepo.getReservationStatuses();

            System.out.println(
                    "Estados cargados: " +
                            reservationStatuses.size()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error cargando estados: " +
                            e.getMessage()
            );

            reservationStatuses = List.of();
        }
    }


    // =========================================================
    // OBTENER NOMBRE CLIENTE
    // =========================================================

    private String obtenerNombreCliente(int idCustomer) {

        for (Customer customer : customers) {

            if (customer.getIdCustomer() == idCustomer) {

                return customer.getName() +
                        " " +
                        customer.getSurname();
            }
        }

        return "Cliente desconocido";
    }


    // =========================================================
    // OBTENER NOMBRE ESTADO
    // =========================================================

    private String obtenerNombreEstado(
            int idReservationStatus) {

        for (ReservationStatus status :
                reservationStatuses) {

            if (status.getIdReservationStatus()
                    == idReservationStatus) {

                return status.getName();
            }
        }

        return "Desconocido";
    }


    // =========================================================
    // CARGAR RESERVAS
    // =========================================================

    private void cargarReservas() {

        try {

            List<Reservation> reservations =
                    reservationRepo.getReservations();

            todasLasReservas.setAll(reservations);

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
    // BÚSQUEDA
    // =========================================================

    private void configurarBusqueda() {

        txtBuscarReserva.textProperty()
                .addListener((observable, oldValue, newValue) -> {

                    String texto =
                            newValue.trim().toLowerCase();

                    if (texto.isEmpty()) {

                        tblReservations.setItems(
                                FXCollections.observableArrayList(
                                        todasLasReservas
                                )
                        );

                        return;
                    }


                    ObservableList<Reservation> filtradas =
                            FXCollections.observableArrayList();


                    for (Reservation reservation :
                            todasLasReservas) {

                        String id =
                                String.valueOf(
                                        reservation.getIdReservation()
                                );

                        String cliente =
                                obtenerNombreCliente(
                                        reservation.getIdCustomer()
                                ).toLowerCase();

                        String estado =
                                obtenerNombreEstado(
                                        reservation.getIdReservationStatus()
                                ).toLowerCase();


                        if (id.contains(texto)
                                || cliente.contains(texto)
                                || estado.contains(texto)) {

                            filtradas.add(reservation);
                        }
                    }


                    tblReservations.setItems(filtradas);
                });
    }


    // =========================================================
    // COLUMNA ACCIONES
    // =========================================================

    private void configurarColumnaAcciones() {

        colActions.setCellFactory(param -> new TableCell<Reservation, Void>() {

            private final Button btnModificar = new Button("Modificar");
            private final Button btnConsumo = new Button("Consumo");
            private final HBox botones = new HBox(8);

            {
                botones.getChildren().addAll(
                        btnModificar,
                        btnConsumo
                );

                botones.setAlignment(Pos.CENTER);

                btnModificar.setOnAction(event -> {

                    Reservation reservation =
                            getTableView().getItems().get(getIndex());

                    handleModificar(reservation);
                });

                btnConsumo.setOnAction(event -> {

                    Reservation reservation =
                            getTableView().getItems().get(getIndex());

                    handleAgregarConsumo(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(botones);
                }
            }
        });
    }

    // =========================================================
    // MODIFICAR RESERVA
    // =========================================================

    private void handleModificar(Reservation reservation) {

        if (reservation == null) {
            return;
        }

        if (dashboardController != null) {

            dashboardController.loadEditReservation(
                    reservation
            );

        } else {

            System.err.println(
                    "DashboardController no está conectado."
            );
        }
    }

    // =========================================================
    // GESTIONAR CONSUMOS
    // =========================================================

    private void handleAgregarConsumo(
            Reservation reservation) {

        System.out.println(
                "Agregar consumo a reserva: " +
                        reservation.getIdReservation()
        );
    }


    // =========================================================
    // CONECTAR CON DASHBOARD
    // =========================================================

    public void setDashboardController(
            DashboardController dashboardController) {

        this.dashboardController =
                dashboardController;
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
    // OBTENER RESERVAS
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




