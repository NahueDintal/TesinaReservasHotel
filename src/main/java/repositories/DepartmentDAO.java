package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DepartmentDAO {

    // LIST ALL (para poblar el ComboBox)
    public Map<Integer, String> listAll() throws SQLException {
        Map<Integer, String> departments = new LinkedHashMap<>();
        String sql = "SELECT id_department, name FROM department ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                departments.put(rs.getInt("id_department"), rs.getString("name"));
            }
        }
        return departments;
    }
}