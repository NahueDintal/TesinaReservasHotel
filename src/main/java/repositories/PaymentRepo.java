package repositories;

import repositories.ConexionDB;
import models.Payment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepo {

    private boolean validatePayment(Payment payment) {

            if (payment == null) {
                System.err.println("El pago ingresado no es válido.");
                return false;
            }

            if (payment.getIdReservation() <= 0) {
                System.err.println("La reserva asociada al pago no es válida.");
                return false;
            }

            if (payment.getAmount() == null ||
                    payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                System.err.println("El importe ingreado debe ser mayor a cero.");
                return false;
            }

            if (payment.getPaymentDate() == null) {
                System.err.println("La fecha del pago es obligatoria.");
                return false;
            }

        if (payment.getIdPaymentMethod() <= 0) {
            System.err.println("Debe seleccionar un método de pago.");
            return false;
        }

        if (payment.getIdPaymentStatus() <= 0) {
            System.err.println("Debe seleccionar un estado de pago.");
            return false;
        }

            if (payment.getObservations() != null &&
                    payment.getObservations().trim().isEmpty()) {
                System.err.println("La observación no puede contener solo espacios.");
                return false;
            }

            return true;
        }

    //para cuando quieras crear un pago independiente
    public boolean createPayment(Payment payment) {

        if (!validatePayment(payment)) {
            return false;
        }

        String sql = "INSERT INTO payment " +
                "(idReservation, amount, paymentDate, idPaymentMethod, idPaymentStatus, observations) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payment.getIdReservation());
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setObject(3, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setInt(4, payment.getIdPaymentMethod());
            stmt.setInt(5, payment.getIdPaymentStatus());
            stmt.setString(6, payment.getObservations());

            int rowsAffected = stmt.executeUpdate();

            if(rowsAffected > 0) {
                System.out.println("Pago creado correctamente.");
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error al crear el pago: " + e.getMessage());
            return false;
        }
    }

    //para cuando el pago forma parte de una transaccion de reserva
    public boolean createPayment(Connection conn, Payment payment) {

        if (!validatePayment(payment)) {
            return false;
        }

        String sql = "INSERT INTO payment " +
                "(idReservation, amount, paymentDate, idPaymentMethod, " +
                "idPaymentStatus, observations) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payment.getIdReservation());
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setTimestamp(
                    3,
                    Timestamp.valueOf(payment.getPaymentDate())
            );
            stmt.setInt(4, payment.getIdPaymentMethod());
            stmt.setInt(5, payment.getIdPaymentStatus());
            stmt.setString(6, payment.getObservations());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(
                        "Pago creado correctamente dentro de la transacción."
                );
                return true;
            }

            return false;

        } catch (SQLException e) {

            System.err.println(
                    "Error al crear el pago dentro de la transacción: "
                            + e.getMessage()
            );

            return false;
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

    public boolean updatePaymentStatus(int idPayment, int idPaymentStatus) {

        if (idPayment <= 0) {
            System.err.println("El ID del pago no es válido.");
            return false;
        }

        if (idPaymentStatus <= 0) {
            System.err.println("El estado del pago no es válido.");
            return false;
        }

        String sql = "UPDATE payment " +
                "SET idPaymentStatus = ? " +
                "WHERE idPayment = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPaymentStatus);
            stmt.setInt(2, idPayment);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println(
                        "Estado del pago actualizado correctamente."
                );
                return true;
            }
            System.out.println(
                    "No se encontró el pago con ID: " + idPayment
            );

            return false;


        } catch (SQLException e) {
            System.err.println(
                    "Error al actualizar el estado del pago: "
                            + e.getMessage()
            );

            return false;
        }
    }

    public boolean updatePayment(Payment payment) {

        if (!validatePayment(payment)) {
            return false;
        }

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
                return true;
            }

            System.out.println("No se encontró el pago con ID: "
                    + payment.getIdPayment());

            return false;



        } catch (SQLException e) {
            System.err.println("Error al actualizar el pago: "
                    + e.getMessage());

            return false;
        }
    }

}