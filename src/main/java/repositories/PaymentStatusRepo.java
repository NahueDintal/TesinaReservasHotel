package repositories;

import repositories.ConexionDB;
import models.PaymentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentStatusRepo {

    public List<PaymentStatus> getPaymentStatuses() {

        List<PaymentStatus> paymentStatuses = new ArrayList<>();

        String sql = "SELECT idPaymentStatus, name " +
                "FROM paymentstatus";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                PaymentStatus paymentStatus = new PaymentStatus();

                paymentStatus.setIdPaymentStatus(
                        rs.getInt("idPaymentStatus")
                );

                paymentStatus.setName(
                        rs.getString("name")
                );

                paymentStatuses.add(paymentStatus);
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al obtener los estados de pago: "
                            + e.getMessage()
            );
        }

        return paymentStatuses;
    }

}
