package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class FeatureDAO {

  public Map<Integer, String> listAll() throws SQLException {
    Map<Integer, String> features = new LinkedHashMap<>();
    String sql = "SELECT id_feature, name FROM feature ORDER BY name";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        features.put(rs.getInt("id_feature"), rs.getString("name"));
      }
    }
    return features;
  }
}
