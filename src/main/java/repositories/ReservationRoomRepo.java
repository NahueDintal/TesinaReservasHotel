package repositories;

import models.ReservationRoom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationRoomRepo {

  // ============================================================
  // CREATE
  // ============================================================
  public void create(ReservationRoom reservationRoom) {
    String sql = "INSERT INTO ReservationRoom (idReservation, roomNumber) VALUES (?, ?)";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, reservationRoom.getIdReservation());
      ps.setInt(2, reservationRoom.getRoomNumber());
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error al guardar la habitación de la reserva", e);
    }
  }

  public void create(Connection conn, ReservationRoom reservationRoom) {
    String sql = "INSERT INTO ReservationRoom (idReservation, roomNumber) VALUES (?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, reservationRoom.getIdReservation());
      ps.setInt(2, reservationRoom.getRoomNumber());
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error al guardar la habitación de la reserva", e);
    }
  }

  // ============================================================
  // READ
  // ============================================================
  /** Devuelve las habitaciones asignadas a una reserva. */
  public List<ReservationRoom> getByReservation(int idReservation) {
    List<ReservationRoom> rooms = new ArrayList<>();

    String sql = "SELECT idReservation, roomNumber FROM ReservationRoom WHERE idReservation = ?";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, idReservation);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          ReservationRoom rr = new ReservationRoom(
              rs.getInt("idReservation"),
              rs.getInt("roomNumber"));
          rooms.add(rr);
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException(
          "Error al obtener habitaciones de la reserva " + idReservation, e);
    }

    return rooms;
  }

  /**
   * Devuelve TODAS las asignaciones reserva-habitación (para la columna del
   * grid).
   */
  public List<ReservationRoom> getAll() {
    List<ReservationRoom> list = new ArrayList<>();
    String sql = "SELECT idReservation, roomNumber FROM ReservationRoom";

    try (Connection conn = ConexionDB.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        list.add(new ReservationRoom(
                rs.getInt("idReservation"),
                rs.getInt("roomNumber")));
      }

    } catch (SQLException e) {
      throw new RuntimeException(
              "Error al obtener las habitaciones de las reservas", e);
    }

    return list;
  }

  // ============================================================
  // DELETE
  // ============================================================
  public void deleteByReservation(Connection conn, int idReservation) {
    String sql = "DELETE FROM ReservationRoom WHERE idReservation = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, idReservation);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException(
          "Error al eliminar las habitaciones de la reserva", e);
    }
  }

  // ============================================================
  // QUERY ESPECIAL: habitaciones ocupadas en un rango de fechas
  // ============================================================
  /**
   * Devuelve los roomNumber ocupados en el rango [checkIn, checkOut).
   * El rango es "exclusivo" en el checkOut (una habitación libera el día que
   * la reserva previa hace check-out), por eso la condición es:
   * existing.checkIn < new.checkOut
   * existing.checkOut > new.checkIn
   *
   * @param idReservationToExclude reserva que se está editando (se excluye para
   *                               no chocar consigo misma)
   */
  public List<Integer> getOccupiedRoomNumbers(LocalDate checkIn,
      LocalDate checkOut,
      Integer idReservationToExclude) {
    List<Integer> occupiedRooms = new ArrayList<>();

    String sql = """
        SELECT DISTINCT rr.roomNumber
        FROM ReservationRoom rr
        JOIN Reservation r ON rr.idReservation = r.idReservation
        WHERE r.checkIn  < ?
          AND r.checkOut > ?
          AND (? IS NULL OR r.idReservation <> ?)
        """;

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setDate(1, java.sql.Date.valueOf(checkOut));
      ps.setDate(2, java.sql.Date.valueOf(checkIn));

      if (idReservationToExclude == null) {
        ps.setNull(3, java.sql.Types.INTEGER);
        ps.setNull(4, java.sql.Types.INTEGER);
      } else {
        ps.setInt(3, idReservationToExclude);
        ps.setInt(4, idReservationToExclude);
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          occupiedRooms.add(rs.getInt("roomNumber"));
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al consultar habitaciones ocupadas", e);
    }

    return occupiedRooms;
  }
}
