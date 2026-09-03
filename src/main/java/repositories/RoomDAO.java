package repositories;

import models.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

  private static final Logger logger = LoggerFactory.getLogger(RoomDAO.class);

  public List<Room> listActive() {
    logger.debug("Listando habitaciones activas");
    return listByActive(true);
  }

  public List<Room> listInactive() {
    return listByActive(false);
  }

  private List<Room> listByActive(boolean active) {
    logger.debug("Ejecutando listByActive con active={}", active);
    List<Room> rooms = new ArrayList<>();
    logger.debug("Ejecutando consulta SELECT");
    String sql = "SELECT r.*, rt.name AS type_name, rv.name AS view_name " +
        "FROM room r " +
        "JOIN room_type rt ON r.id_room_type = rt.id_room_type " +
        "JOIN room_view rv ON r.id_room_view = rv.id_room_view " +
        "WHERE r.active = ?";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setBoolean(1, active);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          Room room = mapRoom(rs);
          room.setFeatures(loadFeaturesForRoom(conn, room.getIdRoom()));
          rooms.add(room);
        }
      }
    } catch (SQLException e) {
      logger.error("Error al listar habitaciones (active={})", active, e);
      throw new RuntimeException("Error al listar habitaciones", e);
    }
    logger.info("Se listaron habitaciones {} (active={})", rooms.size(), active);
    return rooms;
  }

  public Room searchByNumber(int number) {
    logger.debug("Ejecutando searchByNumber con number {}", number);
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
          room.setFeatures(loadFeaturesForRoom(conn, room.getIdRoom()));
          return room;
        }
      }
    } catch (SQLException e) {
      logger.error("Error al obtener habitación por número: {}", number, e);
      throw new RuntimeException("Error al obtener habitación", e);
    }
    logger.info("No se encontro resultados con number {}", number);
    return null;
  }

  public boolean insert(Room room) {
    if (existsActiveByNumber(room.getNumber())) {
      logger.warn("Ya existe una habitación activa con el número {}", room.getNumber());
      throw new IllegalArgumentException("Ya existe una habitación activa con ese número");
    }
    logger.debug("Ejecutando insert con room {}, price {}, type {}, price {}", room.getNumber(), room.getFloor(),
        room.getPrice(), room.getTypeName(), room.getPrice());
    String sqlRoom = "INSERT INTO room (number, floor, id_room_type, capacity, id_room_view, " +
        "available, out_of_service, active, price, description) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?, ?)";
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
        stmt.setBoolean(7, room.isOutOfService());
        stmt.setDouble(8, room.getPrice());
        stmt.setString(9, room.getDescription());
        stmt.executeUpdate();

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            generatedIdRoom = generatedKeys.getInt(1);
            room.setIdRoom(generatedIdRoom);
            logger.info("Habitacion insertada correctamente idRoom {}, number {} ", room.getIdRoom(), room.getNumber());
          } else {
            logger.error("No se pudo obtener el idRoom generado en DB con room {}", room);
            throw new SQLException("No se pudo obtener el idRoom generado.");
          }
        }
      }

      insertFeatures(conn, generatedIdRoom, room.getFeatures());
      conn.commit();
      logger.info("Habitación insertada correctamente: idRoom {}", generatedIdRoom);
      return true;
    } catch (SQLException e) {
      rollback(conn);
      logger.error("Error al insertar habitación", e);
      throw new RuntimeException("Error al insertar habitación", e);
    } finally {
      restoreAutoCommit(conn);
    }
  }

  public boolean update(Room room) {
    logger.debug("ejecutando update de room {}", room.getIdRoom());
    String sqlRoom = "UPDATE room SET number = ?, floor = ?, id_room_type = ?, capacity = ?, " +
        "id_room_view = ?, available = ?, out_of_service = ?, active = ?, " +
        "price = ?, description = ? WHERE idRoom = ?";
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
        stmt.setBoolean(7, room.isOutOfService());
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
      rollback(conn);
      logger.error("Error al actualizar habitación idRoom {}", room.getIdRoom(), e);
      throw new RuntimeException("Error al actualizar habitación", e);
    } finally {
      restoreAutoCommit(conn);
    }
  }

  public boolean delete(int idRoom) {
    logger.debug("ejecutando Soft Delete para room {} ", idRoom);
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

  private Room mapRoom(ResultSet rs) throws SQLException {
    logger.debug("Ejecutando mapRoom");
    Room room = new Room();
    room.setIdRoom(rs.getInt("idRoom"));
    room.setNumber(rs.getInt("number"));
    room.setFloor(rs.getInt("floor"));
    room.setIdRoomType(rs.getInt("id_room_type"));
    room.setTypeName(rs.getString("type_name"));
    room.setCapacity(rs.getInt("capacity"));
    room.setIdRoomView(rs.getInt("id_room_view"));
    room.setViewName(rs.getString("view_name"));
    room.setAvailable(rs.getBoolean("available"));
    room.setOutOfService(rs.getBoolean("out_of_service"));
    room.setActive(rs.getBoolean("active"));
    room.setPrice(rs.getDouble("price"));
    room.setDescription(rs.getString("description"));
    return room;
  }

  private List<String> loadFeaturesForRoom(Connection conn, int idRoom) {
    List<String> features = new ArrayList<>();
    String sql = "SELECT f.name FROM feature f " +
        "JOIN room_feature rf ON f.id_feature = rf.id_feature " +
        "WHERE rf.idRoom = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idRoom);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          features.add(rs.getString("name"));
        }
      }
    } catch (SQLException e) {
      logger.error("No se pudo cargar features para room {}", idRoom);
      throw new RuntimeException("No se pudo cargar las caracteristicas de habitacion", e);
    }
    return features;
  }

  private void insertFeatures(Connection conn, int idRoom, List<String> featureNames) {
    String sql = "INSERT INTO room_feature (idRoom, id_feature) " +
        "SELECT ?, id_feature FROM feature WHERE name = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (String featureName : featureNames) {
        stmt.setInt(1, idRoom);
        stmt.setString(2, featureName);
        stmt.addBatch();
      }
      stmt.executeBatch();
    } catch (SQLException e) {
      logger.error("No se pudo insertar features para room {}", idRoom);
      throw new RuntimeException("No se pude insertar features", e);
    }
  }

  private void deleteFeatures(Connection conn, int idRoom) {
    String sql = "DELETE FROM room_feature WHERE idRoom = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, idRoom);
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("No se pudo borrar las features para la room {}", idRoom);
      throw new RuntimeException("No se pudo borrar las features.", e);
    }
  }

  private void rollback(Connection conn) {
    if (conn != null) {
      try {
        conn.rollback();
      } catch (SQLException ex) {
        logger.error("Error al hacer rollback", ex);
      }
    }
  }

  private void restoreAutoCommit(Connection conn) {
    if (conn != null) {
      try {
        conn.setAutoCommit(true);
      } catch (SQLException ex) {
        logger.error("Error al restaurar autocommit", ex);
      }
    }
  }

  public boolean existsActiveByNumber(int number) {
    String sql = "SELECT COUNT(*) FROM room WHERE number = ? AND active = TRUE";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, number);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    } catch (SQLException e) {
      logger.error("Error al verificar número activo {}", number, e);
      throw new RuntimeException("Error al verificar número de habitación", e);
    }
    return false;
  }
}
