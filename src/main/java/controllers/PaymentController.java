package controllers;

import models.Payment;
import models.PaymentMethod;
import models.PaymentStatus;
import repositories.PaymentRepo;
import repositories.PaymentMethodRepo;
import repositories.PaymentStatusRepo;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;


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

    public PaymentController() {
        paymentRepo = new PaymentRepo();
        paymentMethodRepo = new PaymentMethodRepo();
        paymentStatusRepo = new PaymentStatusRepo();
    }

    @FXML
    public void inicitialize() {
        System.out.println("PaymentControler iniciado");

        loadPaymentMethods();
        loadPaymentStatues();
    }

    private void loadPaymentMethods() {

        List<PaymentMethod> paymentMethods =
                paymentMethodRepo.getPaymentMethods();

        cmbPaymentMethod.getItems().clear();
        cmbPaymentMethod.getItems().addAll(
                paymentMethodRepo.getPaymentMethods()
        );
    }

    private void loadPaymentStatues() {

        List<PaymentStatus> paymentStatuses =
                paymentStatusRepo.getPaymentStatuses();

        cmbPaymentStatus.getItems().clear();
        cmbPaymentStatus.getItems().addAll(
                paymentStatusRepo.getPaymentStatuses()
        );
    }

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

    public boolean updatePaymentStatus(int idPayment, int idPaymentStatus) {
        return paymentRepo.updatePaymentStatus(idPayment, idPaymentStatus);
    }

    @FXML
    private void handleSave() {
        System.out.println("El pago se guardará desde la reserva.");
    }

    @FXML
    private void handleCancel() {
        txtAmount.clear();
        dpPaymentDate.setValue(null);
        cmbPaymentMethod.setValue(null);
        cmbPaymentStatus.setValue(null);
        txtObservations.clear();
    }

    public boolean savePayment(int idReservation) {

        try {

            BigDecimal amount =
                    new BigDecimal(txtAmount.getText());

            LocalDateTime paymentDate;

            if (dpPaymentDate.getValue() != null) {
                paymentDate =
                        dpPaymentDate.getValue().atStartOfDay();
            } else {
                System.out.println(
                        "Debe seleccionar una fecha de pago."
                );
                return false;
            }

            PaymentMethod paymentMethod =
                    cmbPaymentMethod.getValue();

            PaymentStatus paymentStatus =
                    cmbPaymentStatus.getValue();

            if (paymentMethod == null) {
                System.out.println(
                        "Debe seleccionar un método de pago."
                );
                return false;
            }

            if (paymentStatus == null) {
                System.out.println(
                        "Debe seleccionar un estado de pago."
                );
                return false;
            }

            String observations =
                    txtObservations.getText();

            Payment payment = new Payment(
                    idReservation,
                    amount,
                    paymentDate,
                    paymentMethod.getIdPaymentMethod(),
                    paymentStatus.getIdPaymentStatus(),
                    observations
            );

            return paymentRepo.createPayment(payment);

        } catch (NumberFormatException e) {

            System.out.println(
                    "El importe no tiene un formato válido."
            );

            return false;

        } catch (Exception e) {

            System.out.println(
                    "Error al guardar el pago: "
                            + e.getMessage()
            );

            return false;
        }
    }
}
