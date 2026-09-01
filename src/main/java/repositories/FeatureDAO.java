package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class FeatureDAO {

  /**
   * Obtiene todas las características disponibles como un mapa id -> nombre.
   * 
   * @return Map<Integer, String> con id_feature y name.
   * @throws SQLException si ocurre un error de base de datos.
   */
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
