package models;

import java.sql.*;
import java.util.*;

public class CustomerStatusDAO {

    public Map<Integer, String> listarTodos() throws SQLException {
        Map<Integer, String> estados = new LinkedHashMap<>();
        String sql = "SELECT idCustomerStatus, name FROM CustomerStatus ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                estados.put(rs.getInt("idCustomerStatus"), rs.getString("name"));
            }
        }
        return estados;
    }

    public String obtenerNombrePorId(int id) throws SQLException {
        String sql = "SELECT name FROM CustomerStatus WHERE idCustomerStatus = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return null;
    }
}