package models;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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


class PaymentRepo {

    public void createPayment(Payment payment) {
        String sql = "INSERT INTO payment " +
                "(idReservation, amount, paymentDate, idPaymentMethod, idPaymentStatus, observations) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payment.getIdReservation());
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setObject(3, payment.getPaymentDate());
            stmt.setInt(4, payment.getIdPaymentMethod());
            stmt.setInt(5, payment.getIdPaymentStatus());
            stmt.setString(6, payment.getObservations());

            stmt.executeUpdate();

            System.out.println("Pago creado correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al crear el pago: " + e.getMessage());
        }
    }

    public List<Payment> getPayments() {
        List<Payment> payments = new ArrayList<>();

        String sql = "SELECT idPayment, idReservation, amount, paymentDate, " +
                "idPaymentMethod, idPaymentStatus, observations " +
                "FROM payment";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Payment payment = new Payment();

                payment.setIdPayment(rs.getInt("idPayment"));
                payment.setIdReservation(rs.getInt("idReservation"));
                payment.setAmount(rs.getBigDecimal("amount"));

                if (rs.getTimestamp("paymentDate") != null) {
                    payment.setPaymentDate(
                            rs.getTimestamp("paymentDate").toLocalDateTime()
                    );
                }

                payment.setIdPaymentMethod(rs.getInt("idPaymentMethod"));
                payment.setIdPaymentStatus(rs.getInt("idPaymentStatus"));
                payment.setObservations(rs.getString("observations"));

                payments.add(payment);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los pagos: " + e.getMessage());
        }

        return payments;
    }

    public Payment getPaymentById(int idPayment) {

        String sql = "SELECT idPayment, idReservation, amount, paymentDate, " +
                "idPaymentMethod, idPaymentStatus, observations " +
                "FROM payment " +
                "WHERE idPayment = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPayment);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Payment payment = new Payment();

                    payment.setIdPayment(rs.getInt("idPayment"));
                    payment.setIdReservation(rs.getInt("idReservation"));
                    payment.setAmount(rs.getBigDecimal("amount"));

                    if (rs.getTimestamp("paymentDate") != null) {
                        payment.setPaymentDate(
                                rs.getTimestamp("paymentDate").toLocalDateTime()
                        );
                    }

                    payment.setIdPaymentMethod(rs.getInt("idPaymentMethod"));
                    payment.setIdPaymentStatus(rs.getInt("idPaymentStatus"));
                    payment.setObservations(rs.getString("observations"));

                    return payment;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener el pago: " + e.getMessage());
        }

        return null;
    }

    public void updatePaymentStatus(int idPayment, int idPaymentStatus) {

        String sql = "UPDATE payment " +
                "SET idPaymentStatus = ? " +
                "WHERE idPayment = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPaymentStatus);
            stmt.setInt(2, idPayment);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Estado del pago actualizado correctamente.");
            } else {
                System.out.println("No se encontró el pago con ID: " + idPayment);
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado del pago: " + e.getMessage());
        }
    }

    public void updatePayment(Payment payment) {

        String sql = "UPDATE payment SET " +
                "amount = ?, " +
                "paymentDate = ?, " +
                "idPaymentMethod = ?, " +
                "observations = ? " +
                "WHERE idPayment = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, payment.getAmount());
            stmt.setObject(2, payment.getPaymentDate());
            stmt.setInt(3, payment.getIdPaymentMethod());
            stmt.setString(4, payment.getObservations());
            stmt.setInt(5, payment.getIdPayment());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Pago actualizado correctamente.");
            } else {
                System.out.println("No se encontró el pago con ID: " + payment.getIdPayment());
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar el pago: " + e.getMessage());
        }
    }



}
