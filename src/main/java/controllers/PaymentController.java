package controllers;

import models.PaymentMethod;
import models.PaymentStatus;
import repositories.PaymentRepo;
import repositories.PaymentMethodRepo;
import repositories.PaymentStatusRepo;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class PaymentController {


    @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;
    @FXML private ComboBox<PaymentStatus> cmbPaymentStatus;


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

        List<PaymentMethod> paymentMethods = paymentMethodRepo.getPaymentMethods();

        cmbPaymentMethod.getItems().clear();
        cmbPaymentMethod.getItems().addAll(paymentMethods);

    }


    private void loadPaymentStatuses() {

        List<PaymentStatus> paymentStatuses = paymentStatusRepo.getPaymentStatuses();

        cmbPaymentStatus.getItems().clear();
        cmbPaymentStatus.getItems().addAll(paymentStatuses);

    }


}