package controllers;

import models.Payment;
import repositories.PaymentRepo;

import java.util.List;


public class PaymentController {

    private PaymentRepo paymentRepo;

    public PaymentController() {
        paymentRepo = new PaymentRepo();
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

//----------------------------------------------------




}
