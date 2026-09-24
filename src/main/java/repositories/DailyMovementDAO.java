package repositories;

import models.DailyMovement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyMovementDAO {

    /** Returns all check-ins scheduled for the given date. */
    public List<DailyMovement> findCheckInsByDate(LocalDate date) {
        return findByDateAndType(date, "checkIn");
    }

    /** Returns all check-outs scheduled for the given date. */
    public List<DailyMovement> findCheckOutsByDate(LocalDate date) {
        return findByDateAndType(date, "checkOut");
    }

    private List<DailyMovement> findByDateAndType(LocalDate date, String type) {
        List<DailyMovement> result = new ArrayList<>();
        if (date == null) return result;

        // Column name is validated to avoid SQL injection since it can't be parameterized
        String dateColumn = "checkIn".equals(type) ? "r.checkIn" : "r.checkOut";
        String movementType = "checkIn".equals(type) ? "Check-in" : "Check-out";

        String sql =
            "SELECT r.idReservation, " +
            "       CONCAT(c.name, ' ', c.surname) AS customerName, " +
            "       " + dateColumn + " AS movementDate, " +
            "       r.numberOfGuests, " +
            "       r.totalRate, " +
            "       rs.name AS reservationStatus, " +
            "       r.observations " +
            "FROM Reservation r " +
            "JOIN Customer c          ON r.idCustomer = c.idCustomer " +
            "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
            "WHERE " + dateColumn + " = ? " +
            "ORDER BY c.surname, c.name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailyMovement m = new DailyMovement();
                    m.setIdReservation(rs.getInt("idReservation"));
                    m.setCustomerName(rs.getString("customerName"));

                    Date movementDate = rs.getDate("movementDate");
                    if (movementDate != null) {
                        m.setMovementDate(movementDate.toLocalDate());
                    }

                    m.setNumberOfGuests(rs.getInt("numberOfGuests"));
                    m.setTotalRate(rs.getDouble("totalRate"));
                    m.setReservationStatus(rs.getString("reservationStatus"));
                    m.setMovementType(movementType);
                    m.setObservations(rs.getString("observations"));

                    result.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in DailyMovementDAO.findByDateAndType: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}
