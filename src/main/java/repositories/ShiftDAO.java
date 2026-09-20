package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShiftDAO {

  public Map<Integer, String> listAll() throws SQLException {
    Map<Integer, String> shifts = new LinkedHashMap<>();
    String sql = "SELECT idShift, name FROM Shift ORDER BY idShift";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        shifts.put(rs.getInt("idShift"), rs.getString("name"));
      }
    }
    return shifts;
  }
}
