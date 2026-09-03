package repositories;

import models.Probability;
import models.ReservationStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProbabilityDAO {

  private static final Logger logger = LoggerFactory.getLogger(ProbabilityDAO.class);

  public int counterReservationEnded() {
    int count = 0;

    String sql = "COUNT idReservationStatus, 'finalizada' " +
        "FROM ReservationStatus";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        ReservationStatus status = new ReservationStatus();
        status.setIdReservationStatus(rs.getInt("idReservationStatus"));
        status.setName(rs.getString("name"));
      }
    } catch (SQLException e) {
      logger.error("No se pudo concectar con base de datos");
      throw new RuntimeException("No se pudo obtener el conteo de reservas", e);
    }
    return count;
  }

  // cantidad de reservas en finalizada
  // cantidad de reservas canceladas
  // se puede hacer los mismo desde donde vino la reserva, para saber la
  // probabilidad
  // de cancelación si es reserva por bookin, por airbnb, etc

}
