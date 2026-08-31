package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoomTypeDAO {

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
