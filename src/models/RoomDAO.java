package models;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class RoomDAO {

  public Room seachById(int id) {
    String sql = "SELECT id, precio_base, tipo, tiene_balcon, tiene_jacuzzi FROM habitacion WHERE id = ?";
    try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapsRoom(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener la habitación.");
    }
    return null;
  }

  public List<Room> listAll() {
    List<Room> rooms = new ArrayList<>();
    String sql = "SELECT id, ";
    try (Connection conn = ConexionDB.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        room
      }



    } catch (SQLException e ){

    }
  }

  public insert

  public upDate

  public delete

  public
}
