package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class JobPositionDAO {

  public Map<Integer, String> listAll() throws SQLException {
    Map<Integer, String> positions = new LinkedHashMap<>();
    String sql = "SELECT idPosition, name FROM JobPosition ORDER BY name";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        positions.put(rs.getInt("idPosition"), rs.getString("name"));
      }
    }
    return positions;
  }
}
