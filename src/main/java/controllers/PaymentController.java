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

public class PaymentController {

    @FXML
    private TextField txtIDReservation;

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

        try {

            int idReservation = Integer.parseInt(txtIDReservation.getText());
            BigDecimal amount = new BigDecimal(txtAmount.getText());

            LocalDateTime paymentDate;

            if (dpPaymentDate.getValue() != null) {
                paymentDate = dpPaymentDate.getValue().atStartOfDay();
            } else {
                paymentDate = LocalDateTime.now();
            }

            PaymentMethod paymentMethod = cmbPaymentMethod.getValue();
            PaymentStatus paymentStatus = cmbPaymentStatus.getValue();

            String observations = txtObservations.getText();

            if (paymentMethod == null) {
                System.out.println("Debe seleccionar un método de pago.");
                return;
            }

            if (paymentStatus == null) {
                System.out.println("Debe seleccionar un estado de pago.");
                return;
            }

            Payment payment = new Payment(
                    idReservation,
                    amount,
                    paymentDate,
                    paymentMethod.getIdPaymentMethod(),
                    paymentStatus.getIdPaymentStatus(),
                    observations
            );

            boolean success = paymentRepo.createPayment(payment);

            if (success) {
                System.out.println("Pago guardado correctamente.");
            } else {
                System.out.println("No se pudo guardar el pago.");
            }

        } catch (NumberFormatException e) {

            System.out.println("ID de reserva o importe inválido.");

        } catch (Exception e) {

            System.out.println("Error al guardar el pago: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        txtIDReservation.clear();
        txtAmount.clear();
        dpPaymentDate.setValue(null);
        cmbPaymentMethod.setValue(null);
        cmbPaymentStatus.setValue(null);
        txtObservations.clear();
    }

}
