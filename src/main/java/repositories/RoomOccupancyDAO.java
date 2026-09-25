package repositories;

import models.OccupiedInterval;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomOccupancyDAO {

    /**
     * Devuelve los intervalos ocupados por habitación dentro del rango.
     * @return Map<roomNumber, List<OccupiedInterval>>
     */
    public Map<Integer, List<OccupiedInterval>> findOccupiedInRange(LocalDate rangeStart,
                                                                     LocalDate rangeEnd) {
        Map<Integer, List<OccupiedInterval>> result = new HashMap<>();

        String sql =
            "SELECT rr.roomNumber, r.checkIn, r.checkOut " +
            "FROM ReservationRoom rr " +
            "JOIN Reservation r        ON rr.idReservation = r.idReservation " +
            "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
            "WHERE r.checkOut > ? " +
            "  AND r.checkIn  < ? " +
            "  AND LOWER(rs.name) <> 'cancelada' " +
            "ORDER BY rr.roomNumber, r.checkIn";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(rangeStart));
            ps.setDate(2, Date.valueOf(rangeEnd));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int roomNumber = rs.getInt("roomNumber");
                    LocalDate from = rs.getDate("checkIn").toLocalDate();
                    LocalDate to   = rs.getDate("checkOut").toLocalDate();

                    result.computeIfAbsent(roomNumber, k -> new ArrayList<>())
                          .add(new OccupiedInterval(roomNumber, from, to));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar ocupaciones", e);
        }

        return result;
    }
}
