package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoomViewDAO {

  /**
   * Obtiene todas las vistas de habitación como un mapa id -> nombre.
   * 
   * @return Map<Integer, String> con id_room_view y name.
   * @throws SQLException si ocurre un error de base de datos.
   */
  public Map<Integer, String> listAll() throws SQLException {
    Map<Integer, String> views = new LinkedHashMap<>();
    String sql = "SELECT id_room_view, name FROM room_view ORDER BY name";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        views.put(rs.getInt("id_room_view"), rs.getString("name"));
      }
    }
    return views;
  }
}
