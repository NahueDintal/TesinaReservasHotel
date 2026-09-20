package repositories;

import models.ReservationRoom;
import repositories.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ReservationRoomRepo {


    public void create(ReservationRoom reservationRoom) {

        String sql = """
                INSERT INTO ReservationRoom (idReservation, roomNumber)
                VALUES (?, ?)
                """;

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationRoom.getIdReservation());
            ps.setInt(2, reservationRoom.getRoomNumber());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void create(Connection conn, ReservationRoom reservationRoom) {

        String sql = """
            INSERT INTO ReservationRoom (idReservation, roomNumber)
            VALUES (?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationRoom.getIdReservation());
            ps.setInt(2, reservationRoom.getRoomNumber());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la habitación de la reserva", e);
        }
    }

    public List<ReservationRoom> getByReservation(int idReservation) {

        List<ReservationRoom> rooms = new ArrayList<>();

        String sql = """
                SELECT *
                FROM ReservationRoom
                WHERE idReservation = ?
                """;

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idReservation);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                ReservationRoom reservationRoom = new ReservationRoom();

                reservationRoom.setIdReservationRoom(rs.getInt("idReservationRoom"));
                reservationRoom.setIdReservation(rs.getInt("idReservation"));
                reservationRoom.setRoomNumber(rs.getInt("roomNumber"));

                rooms.add(reservationRoom);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    public void deleteByReservation(Connection conn, int idReservation) {

        String sql = """
            DELETE FROM ReservationRoom
            WHERE idReservation = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idReservation);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al eliminar las habitaciones de la reserva",
                    e
            );
        }
    }

    public List<ReservationRoom> getAll() {

        List<ReservationRoom> rooms = new ArrayList<>();

        String sql = """
            SELECT *
            FROM ReservationRoom
            ORDER BY idReservationRoom
            """;

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                ReservationRoom reservationRoom = new ReservationRoom();

                reservationRoom.setIdReservationRoom(rs.getInt("idReservationRoom"));
                reservationRoom.setIdReservation(rs.getInt("idReservation"));
                reservationRoom.setRoomNumber(rs.getInt("roomNumber"));

                rooms.add(reservationRoom);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    public List<Integer> getOccupiedRoomNumbers(
            java.time.LocalDate checkIn,
            java.time.LocalDate checkOut,
            Integer idReservationToExclude) {

        List<Integer> occupiedRooms = new ArrayList<>();

        String sql = """
            SELECT DISTINCT rr.roomNumber
            FROM ReservationRoom rr
            JOIN Reservation r
                ON rr.idReservation = r.idReservation
            WHERE r.checkIn < ?
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
