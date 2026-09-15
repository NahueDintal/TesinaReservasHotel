package repositories;

import models.StaffHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffHistoryDAO {

  // 1. INSERT
  public boolean isInsert(StaffHistory history) throws SQLException {
    String sql = "INSERT INTO StaffHistory (idStaff, changeType, description) VALUES (?, ?, ?)";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, history.getIdStaff());
      stmt.setString(2, history.getChangeType());
      stmt.setString(3, history.getDescription());

      return stmt.executeUpdate() > 0;
    }
  }

  // 2. LIST BY STAFF
  public List<StaffHistory> findByStaffId(String idStaff) throws SQLException {
    List<StaffHistory> historyList = new ArrayList<>();
    String sql = "SELECT * FROM StaffHistory WHERE idStaff = ? ORDER BY changedAt DESC";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, idStaff);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          StaffHistory h = new StaffHistory();
          h.setIdHistory(rs.getInt("idHistory"));
          h.setIdStaff(rs.getString("idStaff"));
          h.setChangeType(rs.getString("changeType"));
          h.setDescription(rs.getString("description"));
          h.setChangedAt(rs.getTimestamp("changedAt").toLocalDateTime());
          historyList.add(h);
        }
      }
    }
    return historyList;
  }
}
