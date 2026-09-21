package repositories;

import models.CancellationImpact;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CancellationImpactDAO {

  private static final String CANCELLED_STATUS = "Cancelada";

  /** Monetary impact of cancellations grouped by booking channel. */
  public List<CancellationImpact> findByChannel(LocalDate fromDate,
      LocalDate toDate,
      String channelFilter) {

    String sql = "SELECT rt.name AS category, " +
        "       COUNT(*) AS total, " +
        "       SUM(CASE WHEN LOWER(rs.name) = LOWER(?) THEN 1 ELSE 0 END) AS cancelled, " +
        "       COALESCE(SUM(r.totalRate), 0) AS total_revenue, " +
        "       COALESCE(SUM(CASE WHEN LOWER(rs.name) = LOWER(?) " +
        "                         THEN r.totalRate ELSE 0 END), 0) AS lost_revenue " +
        "FROM Reservation r " +
        "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
        "JOIN ReservationType   rt ON r.idReservationType   = rt.idReservationType " +
        "WHERE (? IS NULL OR r.checkIn >= ?) " +
        "  AND (? IS NULL OR r.checkIn <= ?) " +
        "  AND (? IS NULL OR rt.name = ?) " +
        "GROUP BY rt.name " +
        "ORDER BY lost_revenue DESC";

    List<CancellationImpact> result = new ArrayList<>();

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      int i = 1;
      ps.setString(i++, CANCELLED_STATUS);
      ps.setString(i++, CANCELLED_STATUS);

      if (fromDate != null) {
        ps.setDate(i++, Date.valueOf(fromDate));
        ps.setDate(i++, Date.valueOf(fromDate));
      } else {
        ps.setNull(i++, Types.DATE);
        ps.setNull(i++, Types.DATE);
      }
      if (toDate != null) {
        ps.setDate(i++, Date.valueOf(toDate));
        ps.setDate(i++, Date.valueOf(toDate));
      } else {
        ps.setNull(i++, Types.DATE);
        ps.setNull(i++, Types.DATE);
      }
      if (channelFilter != null && !channelFilter.isBlank()) {
        ps.setString(i++, channelFilter);
        ps.setString(i, channelFilter);
      } else {
        ps.setNull(i++, Types.VARCHAR);
        ps.setNull(i, Types.VARCHAR);
      }

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          result.add(new CancellationImpact(
              rs.getString("category"),
              rs.getInt("total"),
              rs.getInt("cancelled"),
              rs.getDouble("total_revenue"),
              rs.getDouble("lost_revenue")));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error in CancellationImpactDAO: " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }
}
