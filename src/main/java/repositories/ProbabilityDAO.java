package repositories;

import models.Probability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for cancellation probabilities grouped by reservation type
 * (which acts as the booking channel: Booking, Airbnb, Direct, WhatsApp,
 * Walk-in).
 *
 * Uses the real HotelDataBase schema:
 * - Reservation.idReservationType -> ReservationType.name
 * - Reservation.idReservationStatus -> ReservationStatus.name = 'Cancelada'
 */
public class ProbabilityDAO {

  /** Name used in ReservationStatus to mark a reservation as cancelled. */
  private static final String CANCELLED_STATUS = "Cancelada";

  /**
   * Returns cancellation statistics per reservation type (channel) within the
   * given date range.
   *
   * @param fromDate      start of the period (inclusive). Null means no lower
   *                      bound.
   * @param toDate        end of the period (inclusive). Null means no upper
   *                      bound.
   * @param channelFilter single reservation type to filter by. Null or blank
   *                      means "all".
   * @return list of {@link Probability} rows (one per reservation type).
   */
  public List<Probability> findByPeriod(LocalDate fromDate,
      LocalDate toDate,
      String channelFilter) {

    List<Probability> result = new ArrayList<>();

    String sql = "SELECT rt.name AS channel, " +
        "       COUNT(*) AS total, " +
        "       SUM(CASE WHEN rs.name = ? THEN 1 ELSE 0 END) AS cancelled " +
        "FROM Reservation r " +
        "JOIN ReservationType   rt ON r.idReservationType   = rt.idReservationType " +
        "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
        "WHERE (? IS NULL OR DATE(r.creationDate) >= ?) " +
        "  AND (? IS NULL OR DATE(r.creationDate) <= ?) " +
        "  AND (? IS NULL OR rt.name = ?) " +
        "GROUP BY rt.name " +
        "ORDER BY rt.name";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      int idx = 1;

      // Cancelled status name
      ps.setString(idx++, CANCELLED_STATUS);

      // fromDate
      if (fromDate != null) {
        ps.setDate(idx++, java.sql.Date.valueOf(fromDate));
        ps.setDate(idx++, java.sql.Date.valueOf(fromDate));
      } else {
        ps.setNull(idx++, Types.DATE);
        ps.setNull(idx++, Types.DATE);
      }

      // toDate
      if (toDate != null) {
        ps.setDate(idx++, java.sql.Date.valueOf(toDate));
        ps.setDate(idx++, java.sql.Date.valueOf(toDate));
      } else {
        ps.setNull(idx++, Types.DATE);
        ps.setNull(idx++, Types.DATE);
      }

      // channel filter
      if (channelFilter != null && !channelFilter.isBlank()) {
        ps.setString(idx++, channelFilter);
        ps.setString(idx++, channelFilter);
      } else {
        ps.setNull(idx++, Types.VARCHAR);
        ps.setNull(idx, Types.VARCHAR);
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          String channel = rs.getString("channel");
          int total = rs.getInt("total");
          int cancelled = rs.getInt("cancelled");
          result.add(new Probability(channel, total, cancelled));
        }
      }

    } catch (SQLException e) {
      System.err.println("Error in ProbabilityDAO.findByPeriod: " + e.getMessage());
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Returns the list of reservation types (channels) defined in the system.
   * Useful to populate the channel filter combo box.
   */
  public List<String> findAllChannels() {
    List<String> channels = new ArrayList<>();

    String sql = "SELECT name FROM ReservationType ORDER BY name";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        channels.add(rs.getString("name"));
      }

    } catch (SQLException e) {
      System.err.println("Error in ProbabilityDAO.findAllChannels: " + e.getMessage());
      e.printStackTrace();
    }

    return channels;
  }
}
