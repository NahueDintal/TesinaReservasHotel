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

    public String getNameById(int id) throws SQLException {
        String sql = "SELECT name FROM Country WHERE idCountry = ?";
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