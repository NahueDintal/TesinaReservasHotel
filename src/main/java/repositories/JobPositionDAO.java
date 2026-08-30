package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class JobPositionDAO {

    // LIST ALL (para poblar el ComboBox, igual que DocumentTypeDAO/CountryDAO)
    public Map<Integer, String> listAll() throws SQLException {
        Map<Integer, String> positions = new LinkedHashMap<>();
        String sql = "SELECT id_position, name FROM job_position ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                positions.put(rs.getInt("id_position"), rs.getString("name"));
            }
        }
        return positions;
    }
}