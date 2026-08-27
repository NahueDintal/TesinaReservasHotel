package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import models.Payment;
import models.PaymentMethod;
import models.PaymentStatus;
import models.Reservation;
import models.ReservationStatus;

import repositories.PaymentRepo;
import repositories.PaymentMethodRepo;
import repositories.PaymentStatusRepo;
import repositories.ReservationRepo;
import repositories.ReservationStatusRepo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class NewReservationController {

    // =========================
    // RESERVA
    // =========================

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
    private TextArea txtReservationObservations;


    // =========================
    // PAYMENT
    // =========================

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


    // =========================
    // REPOSITORIES
    // =========================

    private ReservationRepo reservationRepo;
    private ReservationStatusRepo reservationStatusRepo;

    private PaymentRepo paymentRepo;
    private PaymentMethodRepo paymentMethodRepo;
    private PaymentStatusRepo paymentStatusRepo;


    // =========================
    // CONSTRUCTOR
    // =========================

    public NewReservationController() {

        reservationRepo = new ReservationRepo();
        reservationStatusRepo = new ReservationStatusRepo();

        paymentRepo = new PaymentRepo();
        paymentMethodRepo = new PaymentMethodRepo();
        paymentStatusRepo = new PaymentStatusRepo();
    }


    // =========================
    // INITIALIZE
    // =========================

    @FXML
    public void initialize() {

        System.out.println(
                "NewReservationController iniciado"
        );

        loadReservationStatuses();
        loadPaymentMethods();
        loadPaymentStatuses();
    }


    // =========================
    // CARGAR ESTADOS RESERVA
    // =========================

    private void loadReservationStatuses() {

        List<ReservationStatus> statuses =
                reservationStatusRepo.getReservationStatuses();

        cmbReservationStatus.getItems().clear();

        cmbReservationStatus.getItems().addAll(statuses);
    }


    // =========================
    // CARGAR MÉTODOS DE PAGO
    // =========================

    private void loadPaymentMethods() {

        List<PaymentMethod> methods =
                paymentMethodRepo.getPaymentMethods();

        cmbPaymentMethod.getItems().clear();

        cmbPaymentMethod.getItems().addAll(methods);
    }


    // =========================
    // CARGAR ESTADOS DE PAGO
    // =========================

    private void loadPaymentStatuses() {

        List<PaymentStatus> statuses =
                paymentStatusRepo.getPaymentStatuses();

        cmbPaymentStatus.getItems().clear();

        cmbPaymentStatus.getItems().addAll(statuses);
    }


    // =========================
    // GUARDAR RESERVA
    // =========================

    @FXML
    private void handleSave() {

        try {

            // =========================
            // CLIENTE
            // =========================

            Integer idCustomer =
                    cmbCustomer.getValue();

            if (idCustomer == null || idCustomer <= 0) {

                mostrarError(
                        "Debe seleccionar un cliente."
                );

                return;
            }


            // =========================
            // CHECK-IN
            // =========================

            LocalDate checkIn =
                    dpCheckIn.getValue();

            if (checkIn == null) {

                mostrarError(
                        "Debe seleccionar la fecha de check-in."
                );

                return;
            }


            // =========================
            // CHECK-OUT
            // =========================

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
                        "La fecha de check-out debe ser posterior "
                                + "a la fecha de check-in."
                );

                return;
            }


            // =========================
            // CANTIDAD DE HUÉSPEDES
            // =========================

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
                        "La cantidad de huéspedes debe contener "
                                + "solamente números."
                );

                return;
            }


            if (numberOfGuests <= 0) {

                mostrarError(
                        "La cantidad de huéspedes debe ser mayor que 0."
                );

                return;
            }


            // =========================
            // TARIFA TOTAL
            // =========================

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


            // =========================
            // ESTADO DE RESERVA
            // =========================

            ReservationStatus reservationStatus =
                    cmbReservationStatus.getValue();

            if (reservationStatus == null) {

                mostrarError(
                        "Debe seleccionar el estado de la reserva."
                );

                return;
            }


            // =========================
            // OBSERVACIONES RESERVA
            // =========================

            String reservationObservations =
                    txtReservationObservations.getText();

            if (reservationObservations != null &&
                    reservationObservations.trim().isEmpty()) {

                reservationObservations = null;
            }


            // =========================
            // CREAR RESERVA
            // =========================

            Reservation reservation =
                    new Reservation(
                            idCustomer,
                            LocalDateTime.now(),
                            checkIn,
                            checkOut,
                            reservationStatus.getIdReservationStatus(),
                            numberOfGuests,
                            totalRate,
                            reservationObservations
                    );


            int idReservation =
                    reservationRepo.createReservation(reservation);


            // =========================
            // VERIFICAR RESERVA
            // =========================

            if (idReservation <= 0) {

                mostrarError(
                        "No se pudo crear la reserva."
                );

                return;
            }


            // =========================
            // PAYMENT
            // =========================

            String textoPayment =
                    txtPaymentAmount.getText().trim();


            /*
             * El pago es opcional.
             * Si el campo está vacío,
             * simplemente no se crea Payment.
             */

            if (!textoPayment.isEmpty()) {

                BigDecimal paymentAmount;

                try {

                    paymentAmount =
                            new BigDecimal(textoPayment);

                } catch (NumberFormatException e) {

                    mostrarError(
                            "El importe del pago debe contener "
                                    + "solamente números."
                    );

                    return;
                }


                if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {

                    mostrarError(
                            "El importe del pago debe ser mayor que 0."
                    );

                    return;
                }


                // =========================
                // FECHA DEL PAGO
                // =========================

                if (dpPaymentDate.getValue() == null) {

                    mostrarError(
                            "Debe seleccionar la fecha del pago."
                    );

                    return;
                }


                LocalDateTime paymentDate =
                        dpPaymentDate.getValue()
                                .atStartOfDay();


                // =========================
                // MÉTODO DE PAGO
                // =========================

                PaymentMethod paymentMethod =
                        cmbPaymentMethod.getValue();

                if (paymentMethod == null) {

                    mostrarError(
                            "Debe seleccionar el método de pago."
                    );

                    return;
                }


                // =========================
                // ESTADO DEL PAGO
                // =========================

                PaymentStatus paymentStatus =
                        cmbPaymentStatus.getValue();

                if (paymentStatus == null) {

                    mostrarError(
                            "Debe seleccionar el estado del pago."
                    );

                    return;
                }


                // =========================
                // OBSERVACIONES PAYMENT
                // =========================

                String paymentObservations =
                        txtPaymentObservations.getText();

                if (paymentObservations != null &&
                        paymentObservations.trim().isEmpty()) {

                    paymentObservations = null;
                }


                // =========================
                // CREAR PAYMENT
                // =========================

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
                        paymentRepo.createPayment(payment);


                if (!paymentCreated) {

                    mostrarError(
                            "La reserva fue creada, pero "
                                    + "no se pudo registrar el pago."
                    );

                    return;
                }
            }


            // =========================
            // ÉXITO
            // =========================

            mostrarExito(
                    "La reserva se creó correctamente.\n\n"
                            + "Número de reserva: "
                            + idReservation
            );


            limpiarFormulario();

        } catch (Exception e) {

            e.printStackTrace();

            mostrarError(
                    "Ocurrió un error al guardar la reserva:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================
    // CANCELAR
    // =========================

    @FXML
    private void handleCancel() {

        limpiarFormulario();
    }


    // =========================
    // LIMPIAR FORMULARIO
    // =========================

    private void limpiarFormulario() {

        cmbCustomer.setValue(null);

        dpCheckIn.setValue(null);
        dpCheckOut.setValue(null);

        txtNumberOfGuests.clear();
        txtTotalRate.clear();

        cmbReservationStatus.setValue(null);

        txtReservationObservations.clear();

        txtPaymentAmount.clear();

        dpPaymentDate.setValue(null);

        cmbPaymentMethod.setValue(null);
        cmbPaymentStatus.setValue(null);

        txtPaymentObservations.clear();
    }


    // =========================
    // MENSAJE DE ERROR
    // =========================

    private void mostrarError(String mensaje) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText("No se pudo guardar");

        alert.setContentText(mensaje);

        alert.showAndWait();
    }


    // =========================
    // MENSAJE DE ÉXITO
    // =========================

    private void mostrarExito(String mensaje) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Reserva creada");
        alert.setHeaderText("Operación exitosa");

        alert.setContentText(mensaje);

        alert.showAndWait();
    }
}