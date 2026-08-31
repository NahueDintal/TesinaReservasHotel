package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoomViewDAO {

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
