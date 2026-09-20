package repositories;

import models.Staff;
import models.StaffStatus;
import models.StaffHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

  private final StaffHistoryDAO staffHistoryDAO = new StaffHistoryDAO();

  // 1. LIST ALL
  public List<Staff> listAll() throws SQLException {
    List<Staff> staffList = new ArrayList<>();
    String sql = "SELECT s.*, jp.name AS positionName, sh.name AS shiftName " +
        "FROM Staff s " +
        "LEFT JOIN JobPosition jp ON s.idPosition = jp.idPosition " +
        "LEFT JOIN Shift sh ON s.idShift = sh.idShift " +
        "ORDER BY s.id";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        staffList.add(mapRow(rs));
      }
    }
    return staffList;
  }

  // 2. SEARCH BY ID
  public Staff searchById(String id) throws SQLException {
    String sql = "SELECT s.*, jp.name AS positionName, sh.name AS shiftName " +
        "FROM Staff s " +
        "LEFT JOIN JobPosition jp ON s.idPosition = jp.idPosition " +
        "LEFT JOIN Shift sh ON s.idShift = sh.idShift " +
        "WHERE s.id = ?";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapRow(rs);
        }
      }
    }
    return null;
  }

  // 3. INSERT
  public boolean isInsert(Staff staff) throws SQLException {
    if (staff.getId() == null || staff.getId().isEmpty()) {
      staff.setId(generateNextId());
    }
    if (staff.getStatus() == null) {
      staff.setStatus(StaffStatus.ACTIVE);
    }

    String sql = "INSERT INTO Staff (id, firstName, lastName, dni, birthDate, phone, email, " +
        "street, addressNumber, city, idPosition, idShift, status, hireDate, " +
        "shiftStart, shiftEnd, salary) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, staff.getId());
      stmt.setString(2, staff.getFirstName());
      stmt.setString(3, staff.getLastName());
      stmt.setString(4, staff.getDni());
      stmt.setDate(5, staff.getBirthDate() != null ? Date.valueOf(staff.getBirthDate()) : null);
      stmt.setString(6, staff.getPhone());
      stmt.setString(7, staff.getEmail());
      stmt.setString(8, staff.getStreet());
      stmt.setString(9, staff.getAddressNumber());
      stmt.setString(10, staff.getCity());
      stmt.setInt(11, staff.getIdPosition());
      if (staff.getIdShift() != null) {
        stmt.setInt(12, staff.getIdShift());
      } else {
        stmt.setNull(12, Types.INTEGER);
      }
      stmt.setString(13, staff.getStatus().name());
      stmt.setDate(14, staff.getHireDate() != null ? Date.valueOf(staff.getHireDate()) : null);
      stmt.setTime(15, staff.getShiftStart() != null ? Time.valueOf(staff.getShiftStart()) : null);
      stmt.setTime(16, staff.getShiftEnd() != null ? Time.valueOf(staff.getShiftEnd()) : null);
      stmt.setBigDecimal(17, staff.getSalary());

      boolean success = stmt.executeUpdate() > 0;
      if (success) {
        registrarHistorialSinRomperNada(staff.getId(), "CREADO", "Alta de personal: " + staff.getFullName());
      }
      return success;
    }
  }

  // 4. UPDATE
  public boolean isUpdate(Staff staff) throws SQLException {
    String sql = "UPDATE Staff SET firstName=?, lastName=?, dni=?, birthDate=?, phone=?, email=?, " +
        "street=?, addressNumber=?, city=?, idPosition=?, idShift=?, hireDate=?, " +
        "shiftStart=?, shiftEnd=?, salary=? " +
        "WHERE id=?";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, staff.getFirstName());
      stmt.setString(2, staff.getLastName());
      stmt.setString(3, staff.getDni());
      stmt.setDate(4, staff.getBirthDate() != null ? Date.valueOf(staff.getBirthDate()) : null);
      stmt.setString(5, staff.getPhone());
      stmt.setString(6, staff.getEmail());
      stmt.setString(7, staff.getStreet());
      stmt.setString(8, staff.getAddressNumber());
      stmt.setString(9, staff.getCity());
      stmt.setInt(10, staff.getIdPosition());
      if (staff.getIdShift() != null) {
        stmt.setInt(11, staff.getIdShift());
      } else {
        stmt.setNull(11, Types.INTEGER);
      }
      stmt.setDate(12, staff.getHireDate() != null ? Date.valueOf(staff.getHireDate()) : null);
      stmt.setTime(13, staff.getShiftStart() != null ? Time.valueOf(staff.getShiftStart()) : null);
      stmt.setTime(14, staff.getShiftEnd() != null ? Time.valueOf(staff.getShiftEnd()) : null);
      stmt.setBigDecimal(15, staff.getSalary());
      stmt.setString(16, staff.getId());

      boolean success = stmt.executeUpdate() > 0;
      if (success) {
        registrarHistorialSinRomperNada(staff.getId(), "EDITADO", "Datos actualizados de: " + staff.getFullName());
      }
      return success;
    }
  }

  // 5. DELETE
  public boolean isDelete(String id) throws SQLException {
    String sql = "DELETE FROM Staff WHERE id = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, id);
      return stmt.executeUpdate() > 0;
    }
  }

  // 5b. INACTIVAR
  public boolean deactivate(String id) throws SQLException {
    String sql = "UPDATE Staff SET status = 'INACTIVE' WHERE id = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, id);
      boolean success = stmt.executeUpdate() > 0;
      if (success) {
        registrarHistorialSinRomperNada(id, "INACTIVADO", "Empleado inactivado");
      }
      return success;
    }
  }

  // 5c. LISTAR SOLO INACTIVOS
  public List<Staff> findInactive() throws SQLException {
    List<Staff> inactivos = new ArrayList<>();
    String sql = "SELECT s.*, jp.name AS positionName, sh.name AS shiftName " +
        "FROM Staff s " +
        "LEFT JOIN JobPosition jp ON s.idPosition = jp.idPosition " +
        "LEFT JOIN Shift sh ON s.idShift = sh.idShift " +
        "WHERE s.status = 'INACTIVE' " +
        "ORDER BY s.firstName";

    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        inactivos.add(mapRow(rs));
      }
    }
    return inactivos;
  }

  // 5d. REACTIVAR
  public boolean reactivate(String id) throws SQLException {
    String sql = "UPDATE Staff SET status = 'ACTIVE' WHERE id = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, id);
      boolean success = stmt.executeUpdate() > 0;
      if (success) {
        registrarHistorialSinRomperNada(id, "REACTIVADO", "Empleado reactivado");
      }
      return success;
    }
  }

  // 6. FIND BY DNI
  public Staff findByDni(String dni) throws SQLException {
    String sql = "SELECT * FROM Staff WHERE dni = ?";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, dni);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          Staff s = new Staff();
          s.setId(rs.getString("id"));
          s.setFirstName(rs.getString("firstName"));
          s.setLastName(rs.getString("lastName"));
          s.setDni(rs.getString("dni"));
          return s;
        }
      }
    }
    return null;
  }

  private String generateNextId() throws SQLException {
    String sql = "SELECT id FROM Staff ORDER BY id DESC LIMIT 1";
    try (Connection conn = ConexionDB.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        int lastNumber = Integer.parseInt(rs.getString("id"));
        return String.format("%03d", lastNumber + 1);
      }
      return "001";
    }
  }

  private void registrarHistorialSinRomperNada(String idStaff, String changeType, String description) {
    try {
      staffHistoryDAO.isInsert(new StaffHistory(idStaff, changeType, description));
    } catch (SQLException e) {
      System.err.println("No se pudo registrar el historial (no afecta el guardado principal): " + e.getMessage());
    }
  }

  private Staff mapRow(ResultSet rs) throws SQLException {
    Staff s = new Staff();
    s.setId(rs.getString("id"));
    s.setFirstName(rs.getString("firstName"));
    s.setLastName(rs.getString("lastName"));
    s.setDni(rs.getString("dni"));
    s.setBirthDate(rs.getDate("birthDate") != null ? rs.getDate("birthDate").toLocalDate() : null);
    s.setPhone(rs.getString("phone"));
    s.setEmail(rs.getString("email"));
    s.setStreet(rs.getString("street"));
    s.setAddressNumber(rs.getString("addressNumber"));
    s.setCity(rs.getString("city"));
    s.setIdPosition(rs.getInt("idPosition"));
    int idShiftValue = rs.getInt("idShift");
    s.setIdShift(rs.wasNull() ? null : idShiftValue);
    s.setStatus(StaffStatus.valueOf(rs.getString("status")));
    s.setHireDate(rs.getDate("hireDate") != null ? rs.getDate("hireDate").toLocalDate() : null);
    s.setShiftStart(rs.getTime("shiftStart") != null ? rs.getTime("shiftStart").toLocalTime() : null);
    s.setShiftEnd(rs.getTime("shiftEnd") != null ? rs.getTime("shiftEnd").toLocalTime() : null);
    s.setSalary(rs.getBigDecimal("salary"));
    s.setPositionName(rs.getString("positionName"));
    s.setShiftName(rs.getString("shiftName"));
    return s;
  }
}
