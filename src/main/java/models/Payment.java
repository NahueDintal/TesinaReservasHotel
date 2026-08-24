package models;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class Payment {

        private int idPayment;
        private int idReservation;
        private BigDecimal amount;
        private LocalDateTime paymentDate;
        private int idPaymentMethod;
        private int idPaymentStatus;
        private String observations;

    public Payment() {
    }

    public Payment(int idReservation, BigDecimal amount,
                   LocalDateTime paymentDate, int idPaymentMethod,
                   int idPaymentStatus, String observations) {

        this.idReservation = idReservation;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.idPaymentMethod = idPaymentMethod;
        this.idPaymentStatus = idPaymentStatus;
        this.observations = observations;
    }

    public int getIdPayment() {
        return idPayment;
    }
    public void setIdPayment(int idPayment) {
        this.idPayment = idPayment;
    }

    public int getIdReservation() {
        return idReservation;
    }
    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public int getIdPaymentMethod() {
        return idPaymentMethod;
    }
    public void setIdPaymentMethod(int idPaymentMethod) {
        this.idPaymentMethod = idPaymentMethod;
    }

    public int getIdPaymentStatus() {
        return idPaymentStatus;
    }
    public void setIdPaymentStatus(int idPaymentStatus) {
        this.idPaymentStatus = idPaymentStatus;
    }

    public String getObservations() {
        return observations;
    }
    public void setObservations(String observations) {
        this.observations = observations;
    }
}


