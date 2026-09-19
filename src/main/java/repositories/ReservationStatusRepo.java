package repositories;

import models.ReservationStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationStatusRepo {

    public List<ReservationStatus> getReservationStatuses() {

        List<ReservationStatus> statuses = new ArrayList<>();

        String sql = "SELECT idReservationStatus, name " +
                "FROM ReservationStatus";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                ReservationStatus status =
                        new ReservationStatus();

                status.setIdReservationStatus(rs.getInt("idReservationStatus"));
                status.setName(rs.getString("name"));

                statuses.add(status);
            }

        } catch (SQLException e) {

            System.err.println("Error al obtener los estados de reserva: " + e.getMessage());
        }

        return statuses;
    }

}
