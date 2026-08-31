package repositories;

import models.ReservationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReservationTypeRepo {

    public List<ReservationType> getReservationTypes() {

        List<ReservationType> types = new ArrayList<>();

        String sql =
                "SELECT idReservationType, name " +
                        "FROM ReservationType";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                ReservationType type =
                        new ReservationType();

                type.setIdReservationType(
                        rs.getInt("idReservationType")
                );

                type.setName(
                        rs.getString("name")
                );

                types.add(type);
            }

        } catch (Exception e) {

            System.err.println(
                    "Error al obtener los tipos de reserva: "
                            + e.getMessage()
            );
        }

        return types;
    }

    public ReservationType getReservationTypeById(
            int idReservationType) {

        String sql =
                "SELECT idReservationType, name " +
                        "FROM ReservationType " +
                        "WHERE idReservationType = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReservationType);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    ReservationType type =
                            new ReservationType();

                    type.setIdReservationType(
                            rs.getInt("idReservationType")
                    );

                    type.setName(
                            rs.getString("name")
                    );

                    return type;
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "Error al obtener el tipo de reserva: "
                            + e.getMessage()
            );
        }

        return null;
    }
}
