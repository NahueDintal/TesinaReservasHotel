package repositories;

import java.sql.*;
import java.util.*;

public class ProvinceDAO {

    public Map<Integer, String> listAll() throws SQLException {
        Map<Integer, String> provincias = new LinkedHashMap<>();
        String sql = "SELECT idProvince, name FROM Province ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                provincias.put(rs.getInt("idProvince"), rs.getString("name"));
            }
        }
        return provincias;
    }
}
