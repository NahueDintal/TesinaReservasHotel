package repositories;

import models.StaffHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffHistoryDAO {

    // 1. INSERT (se llama automáticamente desde StaffDAO en cada alta/edición/inactivación)
    public boolean isInsert(StaffHistory history) throws SQLException {
        String sql = "INSERT INTO staff_history (id_staff, change_type, description) VALUES (?, ?, ?)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, history.getIdStaff());
            stmt.setString(2, history.getChangeType());
            stmt.setString(3, history.getDescription());

            return stmt.executeUpdate() > 0;
        }
    }

    // 2. LIST BY STAFF (para el botón "Ver Historial")
    public List<StaffHistory> findByStaffId(String idStaff) throws SQLException {
        List<StaffHistory> historyList = new ArrayList<>();
        String sql = "SELECT * FROM staff_history WHERE id_staff = ? ORDER BY changed_at DESC";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idStaff);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StaffHistory h = new StaffHistory();
                    h.setIdHistory(rs.getInt("id_history"));
                    h.setIdStaff(rs.getString("id_staff"));
                    h.setChangeType(rs.getString("change_type"));
                    h.setDescription(rs.getString("description"));
                    h.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
                    historyList.add(h);
                }
            }
        }
        return historyList;
    }
}