package repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoomViewDAO {

  private static final Logger logger = LoggerFactory.getLogger(RoomViewDAO.class);

  public Map<Integer, String> listAll() {
    Map<Integer, String> views = new LinkedHashMap<>();
    String sql = "SELECT idRoomView, name FROM RoomView ORDER BY name";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        views.put(rs.getInt("idRoomView"), rs.getString("name"));
      }
    } catch (SQLException e) {
      logger.error("No se pudo cargar los tipos de vistas de las habitaciones. {}", e.getMessage());
      throw new RuntimeException("No se pudo cargar los tipos de vistas");
    }
    return views;
  }
}
