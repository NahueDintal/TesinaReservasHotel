package repositories;

import java.sql.*;
import java.util.*;

public class CustomerOriginDAO {

    public Map<Integer, String> listAll() throws SQLException {
        Map<Integer, String> origenes = new LinkedHashMap<>();
        String sql = "SELECT idCustomerOrigin, name FROM CustomerOrigin ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                origenes.put(rs.getInt("idCustomerOrigin"), rs.getString("name"));
            }
        }
        return origenes;
    }
}
