package repositories;

import models.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

  private static final Logger logger = LoggerFactory.getLogger(RoomDAO.class);

  // ========== LISTAR ACTIVAS ==========
  public List<Room> listActive() {
    List<Room> rooms = new ArrayList<>();
    String sql = "SELECT r.*, rt.name AS type_name, rv.name AS view_name " +
        "FROM room r " +
        "JOIN room_type rt ON r.id_room_type = rt.id_room_type " +
        "JOIN room_view rv ON r.id_room_view = rv.id_room_view " +
        "WHERE r.active = TRUE";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        Room room = mapRoom(rs);
        room.setFeatures(loadFeaturesForRoom(conn, room.getIdRoom())); // usa idRoom
        rooms.add(room);
      }
    } catch (SQLException e) {
      logger.error("Error al listar habitaciones activas", e);
      throw new RuntimeException("Error al listar habitaciones activas", e);
    }
    return rooms;
  }

  // ========== LISTAR INACTIVAS ==========
  public List<Room> listInactive() {
    List<Room> rooms = new ArrayList<>();
    String sql = "SELECT r.*, rt.name AS type_name, rv.name AS view_name " +
        "FROM room r " +
        "JOIN room_type rt ON r.id_room_type = rt.id_room_type " +
        "JOIN room_view rv ON r.id_room_view = rv.id_room_view " +
        "WHERE r.active = FALSE";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        Room room = mapRoom(rs);
        room.setFeatures(loadFeaturesForRoom(conn, room.getIdRoom()));
        rooms.add(room);
      }
    } catch (SQLException e) {
      logger.error("Error al listar habitaciones inactivas", e);
      throw new RuntimeException("Error al listar habitaciones inactivas", e);
    }
    return rooms;
  }

  // ========== BUSCAR POR NÚMERO ==========
  public Room searchByNumber(int number) {
    String sql = "SELECT r.*, rt.name AS type_name, rv.name AS view_name " +
        "FROM room r " +
        "JOIN room_type rt ON r.id_room_type = rt.id_room_type " +
        "JOIN room_view rv ON r.id_room_view = rv.id_room_view " +
        "WHERE r.number = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, number);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          Room room = mapRoom(rs);
          room.setFeatures(loadFeaturesForRoom(conn, room.getIdRoom())); // antes usaba number
          return room;
        }
      }
    } catch (SQLException e) {
      logger.error("Error al obtener habitación por número: {}", number, e);
      throw new RuntimeException("Error al obtener habitación", e);
    }
    return null;
  }

  // ========== INSERTAR ==========
  public boolean insert(Room room) {
    String sqlRoom = "INSERT INTO room (number, floor, id_room_type, capacity, id_room_view, " +
        "available, out_of_service, active, price, description) VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?, ?)";
    Connection conn = null;
    try {
      conn = ConexionDB.getConnection();
      conn.setAutoCommit(false);

      int generatedIdRoom;
      try (PreparedStatement stmt = conn.prepareStatement(sqlRoom, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, room.getNumber());
        stmt.setInt(2, room.getFloor());
        stmt.setInt(3, room.getIdRoomType());
        stmt.setInt(4, room.getCapacity());
        stmt.setInt(5, room.getIdRoomView());
        stmt.setBoolean(6, room.isAvailable());
        stmt.setBoolean(7, room.getOutOfService());
        stmt.setDouble(8, room.getPrice());
        stmt.setString(9, room.getDescription());
        stmt.executeUpdate();

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            generatedIdRoom = generatedKeys.getInt(1);
            room.setIdRoom(generatedIdRoom); // asignar al objeto
          } else {
            throw new SQLException("No se pudo obtener el idRoom generado.");
          }
        }
      }

      insertFeatures(conn, generatedIdRoom, room.getFeatures()); // usar idRoom

      conn.commit();
      logger.info("Habitación insertada correctamente: idRoom {}", generatedIdRoom);
      return true;
    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          logger.error("Error al hacer rollback", ex);
        }
      }
      logger.error("Error al insertar habitación", e);
      throw new RuntimeException("Error al insertar habitación", e);
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
        } catch (SQLException ex) {
          logger.error("Error al restaurar autocommit", ex);
        }
      }
    }
  }

  // ========== ACTUALIZAR ==========
  public boolean update(Room room) {
    String sqlRoom = "UPDATE room SET number = ?, floor = ?, id_room_type = ?, capacity = ?, id_room_view = ?, " +
        "available = ?, out_of_service = ?, active = ?, price = ?, description = ? WHERE idRoom = ?";
    Connection conn = null;
    try {
      conn = ConexionDB.getConnection();
      conn.setAutoCommit(false);

      try (PreparedStatement stmt = conn.prepareStatement(sqlRoom)) {
        stmt.setInt(1, room.getNumber());
        stmt.setInt(2, room.getFloor());
        stmt.setInt(3, room.getIdRoomType());
        stmt.setInt(4, room.getCapacity());
        stmt.setInt(5, room.getIdRoomView());
        stmt.setBoolean(6, room.isAvailable());
        stmt.setBoolean(7, room.getOutOfService());
        stmt.setBoolean(8, room.isActive());
        stmt.setDouble(9, room.getPrice());
        stmt.setString(10, room.getDescription());
        stmt.setInt(11, room.getIdRoom());
        stmt.executeUpdate();
      }

      deleteFeatures(conn, room.getIdRoom());
      insertFeatures(conn, room.getIdRoom(), room.getFeatures());

      conn.commit();
      logger.info("Habitación actualizada correctamente: idRoom {}", room.getIdRoom());
      return true;
    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          logger.error("Error al hacer rollback", ex);
        }
      }
      logger.error("Error al actualizar habitación idRoom {}", room.getIdRoom(), e);
      throw new RuntimeException("Error al actualizar habitación", e);
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
        } catch (SQLException ex) {
          logger.error("Error al restaurar autocommit", ex);
        }
      }
    }
  }

  // ========== SOFT DELETE ==========
  public boolean delete(int idRoom) {
    String sql = "UPDATE room SET active = FALSE WHERE idRoom = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idRoom);
      int affected = stmt.executeUpdate();
      if (affected > 0) {
        logger.info("Habitación con idRoom {} marcada como inactiva (soft delete).", idRoom);
        return true;
      } else {
        logger.warn("No se encontró habitación con idRoom {} para soft delete.", idRoom);
        return false;
      }
    } catch (SQLException e) {
      logger.error("Error al realizar soft delete de habitación idRoom {}", idRoom, e);
      throw new RuntimeException("Error al eliminar lógicamente la habitación", e);
    }
  }

  // ========== MAPEO ==========
  private Room mapRoom(ResultSet rs) throws SQLException {
    Room room = new Room();
    room.setIdRoom(rs.getInt("idRoom"));
    room.setNumber(String.valueOf(rs.getInt("number")));
    room.setFloor(String.valueOf(rs.getInt("floor")));
    room.setIdRoomType(rs.getInt("id_room_type"));
    room.setTypeName(rs.getString("type_name"));
    room.setCapacity(String.valueOf(rs.getInt("capacity")));
    room.setIdRoomView(rs.getInt("id_room_view"));
    room.setViewName(rs.getString("view_name"));
    room.setAvailable(rs.getBoolean("available"));
    room.setOutOfService(rs.getBoolean("out_of_service"));
    room.setActive(rs.getBoolean("active"));
    room.setPrice(String.valueOf(rs.getDouble("price")));
    room.setDescription(rs.getString("description"));
    return room;
  }

  // ========== CARACTERÍSTICAS ==========
  private List<String> loadFeaturesForRoom(Connection conn, int idRoom) throws SQLException {
    List<String> features = new ArrayList<>();
    String sql = "SELECT f.name FROM feature f " +
        "JOIN room_feature rf ON f.id_feature = rf.id_feature " +
        "WHERE rf.idRoom = ?"; // <-- ahora usa idRoom
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idRoom);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          features.add(rs.getString("name"));
        }
      }
    }
    return features;
  }

  private void insertFeatures(Connection conn, int idRoom, List<String> featureNames) throws SQLException {
    String sql = "INSERT INTO room_feature (idRoom, id_feature) SELECT ?, id_feature FROM feature WHERE name = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (String featureName : featureNames) {
        stmt.setInt(1, idRoom);
        stmt.setString(2, featureName);
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
  }

  private void deleteFeatures(Connection conn, int idRoom) throws SQLException {
    String sql = "DELETE FROM room_feature WHERE idRoom = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idRoom);
      stmt.executeUpdate();
    }
  }
}
