package controllers;

import models.Payment;
import models.PaymentMethod;
import models.PaymentStatus;
import repositories.PaymentRepo;
import repositories.PaymentMethodRepo;
import repositories.PaymentStatusRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class PaymentController {

    @FXML
    private TextField txtAmount;

    @FXML
    private DatePicker dpPaymentDate;

    @FXML
    private ComboBox<PaymentMethod> cmbPaymentMethod;

    @FXML
    private ComboBox<PaymentStatus> cmbPaymentStatus;

    @FXML
    private TextArea txtObservations;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;


    private PaymentRepo paymentRepo;
    private PaymentMethodRepo paymentMethodRepo;
    private PaymentStatusRepo paymentStatusRepo;

    // ID de la reserva asociada al pago
    private int idReservation;


    public PaymentController() {

        paymentRepo = new PaymentRepo();
        paymentMethodRepo = new PaymentMethodRepo();
        paymentStatusRepo = new PaymentStatusRepo();

    }


    // Permite recibir el ID de la reserva desde Reservation
    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }


    @FXML
    public void initialize() {

        System.out.println("PaymentController iniciado");

        loadPaymentMethods();
        loadPaymentStatuses();

    }


    private void loadPaymentMethods() {

        List<PaymentMethod> paymentMethods =
                paymentMethodRepo.getPaymentMethods();

        cmbPaymentMethod.getItems().clear();

        cmbPaymentMethod.getItems().addAll(paymentMethods);

    }


    private void loadPaymentStatuses() {

        List<PaymentStatus> paymentStatuses =
                paymentStatusRepo.getPaymentStatuses();

        cmbPaymentStatus.getItems().clear();

        cmbPaymentStatus.getItems().addAll(paymentStatuses);

    }


    // =========================
    // MÉTODOS DEL ABM
    // =========================

    public boolean createPayment(Payment payment) {

        return paymentRepo.createPayment(payment);

    }


    public List<Payment> getPayments() {

        return paymentRepo.getPayments();

    }


    public Payment getPaymentById(int idPayment) {

        return paymentRepo.getPaymentById(idPayment);

    }


    public boolean updatePayment(Payment payment) {

        return paymentRepo.updatePayment(payment);

    }


    public boolean updatePaymentStatus(
            int idPayment,
            int idPaymentStatus) {

        return paymentRepo.updatePaymentStatus(
                idPayment,
                idPaymentStatus
        );

    }


    // =========================
    // MENSAJE DE ERROR
    // =========================

    private void mostrarError(String mensaje) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");

        alert.setHeaderText("No se pudo guardar el pago");

        alert.setContentText(mensaje);

        alert.showAndWait();

    }


    // =========================
    // GUARDAR PAGO
    // =========================

    @FXML
    private void handleSave() {

        boolean resultado = savePayment(idReservation);

        if (resultado) {

            Alert alert =
                    new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Pago creado");

            alert.setHeaderText(null);

            alert.setContentText(
                    "El pago se creó correctamente."
            );

            alert.showAndWait();

        }

    }


    // =========================
    // CANCELAR
    // =========================

    @FXML
    private void handleCancel() {

        txtAmount.clear();

        dpPaymentDate.setValue(null);

        cmbPaymentMethod.setValue(null);

        cmbPaymentStatus.setValue(null);

        txtObservations.clear();

    }


    // =========================
    // VALIDAR Y CREAR PAYMENT
    // =========================

    public boolean savePayment(int idReservation) {

        try {

            // -------------------------
            // ID DE RESERVA
            // -------------------------

            if (idReservation <= 0) {

                mostrarError(
                        "El ID de reserva no es válido."
                );

                return false;

            }


            // -------------------------
            // IMPORTE
            // -------------------------

            String textoAmount =
                    txtAmount.getText().trim();


            if (textoAmount.isEmpty()) {

                mostrarError(
                        "Debe ingresar un importe."
                );

                return false;

            }


            BigDecimal amount;


            try {

                amount = new BigDecimal(textoAmount);

            } catch (NumberFormatException e) {

                mostrarError(
                        "El importe debe contener solamente números."
                );

                return false;

            }


            // El importe debe ser mayor que 0

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                mostrarError(
                        "El importe debe ser mayor que 0."
                );

                return false;

            }


            // -------------------------
            // FECHA
            // -------------------------

            if (dpPaymentDate.getValue() == null) {

                mostrarError(
                        "Debe seleccionar una fecha de pago."
                );

                return false;

            }


            LocalDateTime paymentDate =
                    dpPaymentDate.getValue().atStartOfDay();


            // -------------------------
            // MÉTODO DE PAGO
            // -------------------------

            PaymentMethod paymentMethod =
                    cmbPaymentMethod.getValue();


            if (paymentMethod == null) {

                mostrarError(
                        "Debe seleccionar un método de pago."
                );

                return false;

            }


            // -------------------------
            // ESTADO DEL PAGO
            // -------------------------

            PaymentStatus paymentStatus =
                    cmbPaymentStatus.getValue();


            if (paymentStatus == null) {

                mostrarError(
                        "Debe seleccionar un estado de pago."
                );

                return false;

            }


            // -------------------------
            // OBSERVACIONES
            // -------------------------

            String observations =
                    txtObservations.getText();


            if (observations != null &&
                    observations.trim().isEmpty()) {

                observations = null;

            }


            // -------------------------
            // CREAR PAYMENT
            // -------------------------

            Payment payment = new Payment(

                    idReservation,

                    amount,

                    paymentDate,

                    paymentMethod.getIdPaymentMethod(),

                    paymentStatus.getIdPaymentStatus(),

                    observations

            );


            // -------------------------
            // GUARDAR EN BASE DE DATOS
            // -------------------------

            boolean resultado =
                    paymentRepo.createPayment(payment);


            if (!resultado) {

                mostrarError(
                        "No se pudo guardar el pago."
                );

            }


            return resultado;


        } catch (Exception e) {

            mostrarError(
                    "Ocurrió un error al guardar el pago."
            );

            return false;

        }

    }

}