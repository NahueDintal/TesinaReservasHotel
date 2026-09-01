package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoomTypeDAO {

  /**
   * Obtiene todos los tipos de habitación como un mapa id -> nombre.
   * 
   * @return Map<Integer, String> con id_room_type y name.
   * @throws SQLException si ocurre un error de base de datos.
   */
  public Map<Integer, String> listAll() throws SQLException {
    Map<Integer, String> types = new LinkedHashMap<>();
    String sql = "SELECT id_room_type, name FROM room_type ORDER BY name";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        types.put(rs.getInt("id_room_type"), rs.getString("name"));
      }
    }
    return types;
  }
}
