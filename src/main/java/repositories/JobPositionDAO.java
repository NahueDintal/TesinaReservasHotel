package repositories;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class JobPositionDAO {

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

    // Devuelve el id_department que corresponde a un cargo (para derivar el Área automáticamente)
    public Integer getDepartmentIdByPosition(int idPosition) throws SQLException {
        String sql = "SELECT id_department FROM job_position WHERE id_position = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPosition);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_department");
                }
            }
        }
        return null;
    }
}