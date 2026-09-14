package repositories;

import java.sql.*;
import java.util.*;

public class CustomerStatusDAO {

    public Map<Integer, String> listAll() throws SQLException {
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
}