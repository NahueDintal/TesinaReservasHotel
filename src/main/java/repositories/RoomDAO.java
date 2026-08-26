package repositories;

import models.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

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
            throw new RuntimeException("Error al listar las habitaciones.", e);
        }
        return rooms;
    }

    public void insert(Room room) {
        String sql = "INSERT INTO room (number, floor, type, capacity, view, available, out_of_service, features, price, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, room.getNumber());
            pstmt.setInt(2, room.getFloor());
            pstmt.setString(3, room.getType());
            pstmt.setInt(4, room.getCapacity());
            pstmt.setString(5, room.getView());          // corregido
            pstmt.setBoolean(6, room.getIsAvailable());
            pstmt.setBoolean(7, room.getOutOfService());
            pstmt.setString(8, room.getFeatures());
            pstmt.setDouble(9, room.getPrice());
            pstmt.setString(10, room.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar habitación.", e);
        }
    }

    public void update(Room room) {
        String sql = "UPDATE room SET floor = ?, type = ?, capacity = ?, view = ?, available = ?, " +
                     "out_of_service = ?, features = ?, price = ?, description = ? WHERE number = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, room.getFloor());
            pstmt.setString(2, room.getType());
            pstmt.setInt(3, room.getCapacity());
            pstmt.setString(4, room.getView());
            pstmt.setBoolean(5, room.getIsAvailable());
            pstmt.setBoolean(6, room.getOutOfService());
            pstmt.setString(7, room.getFeatures());
            pstmt.setDouble(8, room.getPrice());
            pstmt.setString(9, room.getDescription());
            pstmt.setInt(10, room.getNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar habitación.", e);
        }
    }

    public void delete(int number) {
        String sql = "DELETE FROM room WHERE number = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, number);                    // corregido
            pstmt.executeUpdate();
        } catch (SQLException e) {
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

        Room room = new Room(numberStr, floorStr, type, capacityStr, view, features, priceStr, description);
        room.setIsAvailable(rs.getBoolean("available"));
        room.setOutOfService(rs.getBoolean("out_of_service"));
        return room;
    }
}
