package repositories;

import java.sql.*;
import java.util.*;

public class CountryDAO {

    public Map<Integer, String> listAll() throws SQLException {
        Map<Integer, String> paises = new LinkedHashMap<>();
        String sql = "SELECT idCountry, name FROM Country ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                paises.put(rs.getInt("idCountry"), rs.getString("name"));
            }
        }
        return paises;
    }
}