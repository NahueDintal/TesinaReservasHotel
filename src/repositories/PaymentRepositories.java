package repositories;

import models.Payment;

import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentRepositories {

    public void createPayment (Payment payment) {

        String sql = """
                INSERT INTO Payment
                (idReservation, amount, paymentDate,
                idPaymentMethod,idPaymentStatus, observations)
                VALUES (?,?,?,?,?,?)
                """;

        try {

            Connection connection = /* PENDIENTE */;

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, payment.getIdReservation());
            statement.setBigDecimal(2, payment.getAmount());
            statement.setObject(3, payment.getPaymentDate());
            statement.setInt(4, payment.getIdPaymentMethod());
            statement.setInt(5, payment.getIdPaymentStatus());
            statement.setString(6, payment.getObservations());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public Payment getPaymentById(int idPayment) {

        String sql = """
                SELECT idPayment, idReservation, amount,
                paymentDate, idPaymentMethod, idPaymentStatus,
                observations
                FROM Payment
                WHERE idPayment = ?
                """;

        try {

            Connection connection = /*conecion */;

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, idPayment);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {

                Payment payment = new Payment();

                payment.setIdPayment(
                        resultSet.getInt("idPayment"));

                payment.setIdReservation(
                        resultSet.getInt("idReservation"));

                payment.setAmount(
                        resultSet.getBigDecimal("amount"));

                payment.setPaymentDate(
                        resultSet.getTimestamp("paymentDate")
                                .toLocalDateTime());

                payment.setIdPaymentMethod(
                        resultSet.getInt("idPaymentMethod"));

                payment.setIdPaymentStatus(
                        resultSet.getInt("idPaymentStatus"));

                payment.setObservations(
                        resultSet.getString("observations"));

                return payment;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Payment> getAllPayments() {

        String sql = """
            SELECT idPayment, idReservation, amount,
                   paymentDate, idPaymentMethod,
                   idPaymentStatus, observations
            FROM Payment
            """;

        List<Payment> payments = new ArrayList<>();

        try {

            Connection connection = /* conexión pendiente */;

            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Payment payment = new Payment();

                payment.setIdPayment(
                        resultSet.getInt("idPayment"));

                payment.setIdReservation(
                        resultSet.getInt("idReservation"));
                payment.setAmount(
                        resultSet.getBigDecimal("amount"));

                payment.setPaymentDate(
                        resultSet.getTimestamp("paymentDate")
                                .toLocalDateTime());

                payment.setIdPaymentMethod(
                        resultSet.getInt("idPaymentMethod"));

                payment.setIdPaymentStatus(
                        resultSet.getInt("idPaymentStatus"));

                payment.setObservations(
                        resultSet.getString("observations"));

                payments.add(payment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    public void updatePayment (Payment payment) {

    }

    public void deletePayment (int idPayment) {

    }
    //----------------------------------------------------------------







}


























