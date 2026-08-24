package main.java.models;

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
          return mapRoom(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener la habitación por id.");
    }
    return null;
  }

  public List<Room> listAll() {
    List<Room> rooms = new ArrayList<>();
    String sql = "SELECT * FROM room";
    try (Connection conn = ConexionDB.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        rooms.add(mapRoom(rs));
      }
    } catch (SQLException e ){
      throw new RuntimeException("Error al listar las habitaciones.")
    }
    return rooms;
  }

  public insert() {
    String sql = "";
    try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapRoom(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al insertar la habitación.");
    }
    return null;
  }

  }

  public upDate

  public delete

  private Room mapRoom(ResultSet rs) throws SQLException {
    String numberStr = rs.getString("number");
    String floorStr = rs.getString("floor");
    String type = rs.getString("type");
    String capacityStr = rs.getString("capacity");
    String view = rs.getString("view");
    String availableStr = rs.getString("available");
    String features = rs.getString("features");
    String priceStr = rs.getString("price");
    String description = rs.getString("description");

    return new Room(numberStr, floorStr, type, capacityStr, view, availableStr, features, priceStr, description);
  }
}
