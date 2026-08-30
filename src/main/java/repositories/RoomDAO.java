package repositories;

import models.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

  private static final Logger logger = LoggerFactory.getLogger(RoomDAO.class);

  public Room searchByNumber(int number) {
    String sql = "SELECT * FROM room WHERE number = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, number);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return mapRoom(rs);
        }
      }
    } catch (SQLException e) {
      logger.error("Error al obtener habitación por número: {}", number, e);
      throw new RuntimeException("Error al obtener la habitación por número.", e);
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
    } catch (SQLException e) {
      logger.error("Error al listar habitaciones", e);
      throw new RuntimeException("Error al listar las habitaciones.", e);
    }
    return rooms;
  }

  public boolean insert(Room room) {
    String sql = "INSERT INTO room (number, floor, type, capacity, view, available, out_of_service, features, price, description) "
        +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, room.getNumber());
      pstmt.setInt(2, room.getFloor());
      pstmt.setString(3, room.getType());
      pstmt.setInt(4, room.getCapacity());
      pstmt.setString(5, room.getView());
      pstmt.setBoolean(6, room.isAvailable());
      pstmt.setBoolean(7, room.getOutOfService());
      pstmt.setString(8, room.getFeatures());
      pstmt.setDouble(9, room.getPrice());
      pstmt.setString(10, room.getDescription());
      pstmt.executeUpdate();
      logger.info("Habitación insertada correctamente: número {}", room.getNumber());
      return true;
    } catch (SQLException e) {
      logger.error("Error al insertar habitación número {}", room.getNumber(), e);
      throw new RuntimeException("Error al insertar habitación.", e);
    }
  }

  public boolean update(Room room) {
    String sql = "UPDATE room SET floor = ?, type = ?, capacity = ?, view = ?, available = ?, " +
        "out_of_service = ?, features = ?, price = ?, description = ? WHERE number = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, room.getFloor());
      pstmt.setString(2, room.getType());
      pstmt.setInt(3, room.getCapacity());
      pstmt.setString(4, room.getView());
      pstmt.setBoolean(5, room.isAvailable());
      pstmt.setBoolean(6, room.getOutOfService());
      pstmt.setString(7, room.getFeatures());
      pstmt.setDouble(8, room.getPrice());
      pstmt.setString(9, room.getDescription());
      pstmt.setInt(10, room.getNumber());
      pstmt.executeUpdate();
      logger.info("Habitación actualizada correctamente: número {}", room.getNumber());
      return true;
    } catch (SQLException e) {
      logger.error("Error al actualizar habitación número {}", room.getNumber(), e);
      throw new RuntimeException("Error al actualizar habitación.", e);
    }
  }

  public void delete(int number) {
    String sql = "DELETE FROM room WHERE number = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, number);
      pstmt.executeUpdate();
      logger.info("Habitación eliminada correctamente: número {}", number);
    } catch (SQLException e) {
      logger.error("Error al borrar habitación número {}", number, e);
      throw new RuntimeException("Error al borrar habitación.", e);
    }
  }

  private Room mapRoom(ResultSet rs) throws SQLException {
    String numberStr = rs.getString("number");
    String floorStr = rs.getString("floor");
    String type = rs.getString("type");
    String capacityStr = rs.getString("capacity");
    String view = rs.getString("view");
    String features = rs.getString("features");
    String priceStr = rs.getString("price");
    String description = rs.getString("description");

    // Construir Room (los setters lanzan excepción si algo falla)
    Room room = new Room(numberStr, floorStr, type, capacityStr, view, features, priceStr, description);

    // Asignar disponibilidad y estado fuera de servicio
    room.setAvailable(rs.getBoolean("available"));
    room.setOutOfService(rs.getBoolean("out_of_service"));
    return room;
  }
}
