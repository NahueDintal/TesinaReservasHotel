package repositories;

import models.Probability;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProbabilityDAO {

  private static final String CANCELLED_STATUS = "Cancelada";

  public List<Probability> findByChannel(LocalDate fromDate,
      LocalDate toDate,
      String channelFilter) {

    String sql = "SELECT rt.name AS category, " +
        "       COUNT(*) AS total, " +
        "       SUM(CASE WHEN LOWER(rs.name) = LOWER(?) THEN 1 ELSE 0 END) AS cancelled " +
        "FROM Reservation r " +
        "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
        "JOIN ReservationType   rt ON r.idReservationType   = rt.idReservationType " +
        "WHERE (? IS NULL OR r.checkIn >= ?) " +
        "  AND (? IS NULL OR r.checkIn <= ?) " +
        "  AND (? IS NULL OR rt.name = ?) " +
        "GROUP BY rt.name " +
        "ORDER BY rt.name";

    return runQuery(sql, fromDate, toDate, channelFilter);
  }

  public List<Probability> findByLeadTime(LocalDate fromDate,
      LocalDate toDate,
      String channelFilter) {

    String sql = "SELECT CASE " +
        "         WHEN DATEDIFF(r.checkIn, DATE(r.creationDate)) <= 7  THEN '0-7 days' " +
        "         WHEN DATEDIFF(r.checkIn, DATE(r.creationDate)) <= 30 THEN '8-30 days' " +
        "         WHEN DATEDIFF(r.checkIn, DATE(r.creationDate)) <= 60 THEN '31-60 days' " +
        "         WHEN DATEDIFF(r.checkIn, DATE(r.creationDate)) <= 90 THEN '61-90 days' " +
        "         ELSE '90+ days' " +
        "       END AS category, " +
        "       CASE " +
        "         WHEN DATEDIFF(r.checkIn, DATE(r.creationDate)) <= 7  THEN 1 " +
        "         WHEN DATEDIFF(r.checkIn, DATE(r.creationDate)) <= 30 THEN 2 " +
        "         WHEN DATEDIFF(r.checkIn, DATE(r.creationDate)) <= 60 THEN 3 " +
        "         WHEN DATEDIFF(r.checkIn, DATE(r.creationDate)) <= 90 THEN 4 " +
        "         ELSE 5 " +
        "       END AS bucket_order, " +
        "       COUNT(*) AS total, " +
        "       SUM(CASE WHEN LOWER(rs.name) = LOWER(?) THEN 1 ELSE 0 END) AS cancelled " +
        "FROM Reservation r " +
        "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
        "JOIN ReservationType   rt ON r.idReservationType   = rt.idReservationType " +
        "WHERE (? IS NULL OR r.checkIn >= ?) " +
        "  AND (? IS NULL OR r.checkIn <= ?) " +
        "  AND (? IS NULL OR rt.name = ?) " +
        "GROUP BY category, bucket_order " +
        "ORDER BY bucket_order";

    return runQuery(sql, fromDate, toDate, channelFilter);
  }

  public List<String> findAllChannels() {
    List<String> channels = new ArrayList<>();
    String sql = "SELECT name FROM ReservationType ORDER BY name";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next())
        channels.add(rs.getString("name"));
    } catch (SQLException e) {
      System.err.println("Error in ProbabilityDAO.findAllChannels: " + e.getMessage());
    }
    return channels;
  }

  private List<Probability> runQuery(String sql, LocalDate fromDate,
      LocalDate toDate, String channelFilter) {
    List<Probability> result = new ArrayList<>();
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      int i = 1;
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
          result.add(new Probability(
              rs.getString("category"),
              rs.getInt("total"),
              rs.getInt("cancelled")));
        }
      }
    } catch (SQLException e) {
      System.err.println("Error in ProbabilityDAO: " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }

  public List<Probability> findByMonth(LocalDate fromDate,
      LocalDate toDate,
      String channelFilter) {

    String sql = "SELECT MONTHNAME(r.checkIn) AS category, " +
        "       MONTH(r.checkIn)     AS month_order, " +
        "       COUNT(*) AS total, " +
        "       SUM(CASE WHEN LOWER(rs.name) = LOWER(?) THEN 1 ELSE 0 END) AS cancelled " +
        "FROM Reservation r " +
        "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
        "JOIN ReservationType   rt ON r.idReservationType   = rt.idReservationType " +
        "WHERE (? IS NULL OR r.checkIn >= ?) " +
        "  AND (? IS NULL OR r.checkIn <= ?) " +
        "  AND (? IS NULL OR rt.name = ?) " +
        "GROUP BY category, month_order " +
        "ORDER BY month_order";

    return runQuery(sql, fromDate, toDate, channelFilter);
  }

  public List<Probability> findByGuests(LocalDate fromDate,
      LocalDate toDate,
      String channelFilter) {

    String sql = "SELECT CASE " +
        "         WHEN r.numberOfGuests = 1 THEN '1 huésped' " +
        "         WHEN r.numberOfGuests = 2 THEN '2 huéspedes' " +
        "         WHEN r.numberOfGuests BETWEEN 3 AND 4 THEN '3-4 huéspedes' " +
        "         ELSE '5+ huéspedes' " +
        "       END AS category, " +
        "       CASE " +
        "         WHEN r.numberOfGuests = 1 THEN 1 " +
        "         WHEN r.numberOfGuests = 2 THEN 2 " +
        "         WHEN r.numberOfGuests BETWEEN 3 AND 4 THEN 3 " +
        "         ELSE 4 " +
        "       END AS bucket_order, " +
        "       COUNT(*) AS total, " +
        "       SUM(CASE WHEN LOWER(rs.name) = LOWER(?) THEN 1 ELSE 0 END) AS cancelled " +
        "FROM Reservation r " +
        "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
        "JOIN ReservationType   rt ON r.idReservationType   = rt.idReservationType " +
        "WHERE (? IS NULL OR r.checkIn >= ?) " +
        "  AND (? IS NULL OR r.checkIn <= ?) " +
        "  AND (? IS NULL OR rt.name = ?) " +
        "GROUP BY category, bucket_order " +
        "ORDER BY bucket_order";

    return runQuery(sql, fromDate, toDate, channelFilter);
  }

  public List<Probability> findByCustomerOrigin(LocalDate fromDate,
      LocalDate toDate,
      String channelFilter) {

    String sql = "SELECT co.name AS category, " +
        "       COUNT(*) AS total, " +
        "       SUM(CASE WHEN LOWER(rs.name) = LOWER(?) THEN 1 ELSE 0 END) AS cancelled " +
        "FROM Reservation r " +
        "JOIN ReservationStatus rs ON r.idReservationStatus = rs.idReservationStatus " +
        "JOIN ReservationType   rt ON r.idReservationType   = rt.idReservationType " +
        "JOIN Customer          c  ON r.idCustomer          = c.idCustomer " +
        "JOIN CustomerOrigin    co ON c.idCustomerOrigin    = co.idCustomerOrigin " +
        "WHERE (? IS NULL OR r.checkIn >= ?) " +
        "  AND (? IS NULL OR r.checkIn <= ?) " +
        "  AND (? IS NULL OR rt.name = ?) " +
        "GROUP BY co.name " +
        "ORDER BY co.name";

    return runQuery(sql, fromDate, toDate, channelFilter);
  }
}
