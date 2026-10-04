package repositories;

import models.OccupiedInterval;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomOccupancyDAO {
  public Map<Integer, List<OccupiedInterval>> findOccupiedInRange(LocalDate rangeStart,
      LocalDate rangeEnd) {
    Map<Integer, List<OccupiedInterval>> result = new HashMap<>();

    String sql = "SELECT room.number AS roomNumber, res.checkIn, res.checkOut " +
        "FROM ReservationRoom rr " +
        "JOIN Reservation res        ON rr.idReservation = res.idReservation " +
        "JOIN ReservationStatus rs   ON res.idReservationStatus = rs.idReservationStatus " +
        "JOIN Room room              ON room.idRoom = rr.idRoom " +
        "WHERE res.checkOut > ? " +
        "  AND res.checkIn  < ? " +
        "  AND LOWER(rs.name) <> 'cancelada' " +
        "ORDER BY room.number, res.checkIn";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setDate(1, Date.valueOf(rangeStart));
      ps.setDate(2, Date.valueOf(rangeEnd));

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          int roomNumber = rs.getInt("roomNumber"); // 👈 ahora sí es number
          LocalDate from = rs.getDate("checkIn").toLocalDate();
          LocalDate to = rs.getDate("checkOut").toLocalDate();

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
