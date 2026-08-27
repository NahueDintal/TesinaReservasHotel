package repositories;

import models.Consumption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConsumptionRepo {

    public boolean createConsumption(
            Connection conn,
            Consumption consumption
    ) throws SQLException {

        String sql =
                "INSERT INTO Consumption (" +
                        "idReservation, " +
                        "idConsumptionType, " +
                        "idProduct, " +
                        "idService, " +
                        "quantity, " +
                        "unitPrice, " +
                        "total, " +
                        "consumptionDate, " +
                        "idPaymentStatus, " +
                        "observations" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    consumption.getIdReservation()
            );

            ps.setInt(
                    2,
                    consumption.getIdConsumptionType()
            );

            ps.setInt(
                    3,
                    consumption.getIdProduct()
            );

            ps.setInt(
                    4,
                    consumption.getIdService()
            );

            ps.setInt(
                    5,
                    consumption.getQuantity()
            );

            ps.setBigDecimal(
                    6,
                    consumption.getUnitPrice()
            );

            ps.setBigDecimal(
                    7,
                    consumption.getTotal()
            );

            ps.setObject(
                    8,
                    consumption.getConsumptionDate()
            );

            ps.setInt(
                    9,
                    consumption.getIdPaymentStatus()
            );

            ps.setString(
                    10,
                    consumption.getObservations()
            );

            return ps.executeUpdate() > 0;
        }
    }
}