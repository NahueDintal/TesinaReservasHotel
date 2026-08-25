package repositories;

import models.ConexionDB;
import models.PaymentMethod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodRepo {

    public List<PaymentMethod> getPaymentMethods() {

        List<PaymentMethod> paymentMethods = new ArrayList<>();

        String sql = "SELECT idPaymentMethod, name " +
                "FROM paymentmethod";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                PaymentMethod paymentMethod = new PaymentMethod();

                paymentMethod.setIdPaymentMethod(
                        rs.getInt("idPaymentMethod")
                );

                paymentMethod.setName(
                        rs.getString("name")
                );

                paymentMethods.add(paymentMethod);
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al obtener los métodos de pago: "
                            + e.getMessage()
            );
        }

        return paymentMethods;
    }

}
