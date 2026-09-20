package repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoomTypeDAO {

  private static final Logger logger = LoggerFactory.getLogger(RoomTypeDAO.class);

  public Map<Integer, String> listAll() {
    Map<Integer, String> types = new LinkedHashMap<>();
    String sql = "SELECT idRoomType, name FROM RoomType ORDER BY name";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        types.put(rs.getInt("idRoomType"), rs.getString("name"));
      }
    } catch (SQLException e) {
      logger.error("No se puede listar los tipos de habitación. {}", e.getMessage());
      throw new RuntimeException("No se pudo cargar el tipo de habitación");
    }
    return types;
  }
}
