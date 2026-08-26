package repositories;

import java.sql.*;
import java.util.*;

public class DocumentTypeDAO {

    public Map<Integer, String> listAll() throws SQLException {
        Map<Integer, String> tipos = new LinkedHashMap<>();  // LinkedHashMap mantiene el orden
        String sql = "SELECT idDocumentType, name FROM DocumentType ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tipos.put(rs.getInt("idDocumentType"), rs.getString("name"));
            }
        }
        return tipos;
    }

    // Metodo para obtener un nombre a partir de un ID (útil si necesitás mostrar en la tabla)
    public String getNameById(int id) throws SQLException {
        String sql = "SELECT name FROM DocumentType WHERE idDocumentType = ?";
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