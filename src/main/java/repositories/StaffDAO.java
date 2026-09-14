package repositories;

import models.Staff;
import models.StaffStatus;
import models.StaffHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    private final StaffHistoryDAO staffHistoryDAO = new StaffHistoryDAO();

    // 1. LIST ALL — Área se obtiene vía job_position -> department; Turno vía LEFT JOIN (puede ser null)
    public List<Staff> listAll() throws SQLException {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT s.*, jp.name AS positionName, sh.name AS shiftName " +
                "FROM staff s " +
                "LEFT JOIN job_position jp ON s.id_position = jp.id_position " +
                "LEFT JOIN shift sh ON s.id_shift = sh.id_shift " +
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
                "FROM staff s " +
                "LEFT JOIN job_position jp ON s.id_position = jp.id_position " +
                "LEFT JOIN shift sh ON s.id_shift = sh.id_shift " +
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
            staff.setStatus(StaffStatus.ACTIVE); // por defecto, todo alta nace Activo
        }

        String sql = "INSERT INTO staff (id, first_name, last_name, dni, birth_date, phone, email, " +
                "street, address_number, city, id_position, id_shift, status, hire_date, " +
                "shift_start, shift_end, salary) " +
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
        String sql = "UPDATE staff SET first_name=?, last_name=?, dni=?, birth_date=?, phone=?, email=?, " +
                "street=?, address_number=?, city=?, id_position=?, id_shift=?, hire_date=?, " +
                "shift_start=?, shift_end=?, salary=? " +
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
        String sql = "DELETE FROM staff WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // 5b. INACTIVAR
    public boolean deactivate(String id) throws SQLException {
        String sql = "UPDATE staff SET status = 'INACTIVE' WHERE id = ?";
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

    // 5c. LISTAR SOLO INACTIVOS (para la ventana "Historial" -> Personal Inactivo)
    public List<Staff> findInactive() throws SQLException {
        List<Staff> inactivos = new ArrayList<>();
        String sql = "SELECT s.*, jp.name AS positionName, sh.name AS shiftName " +
                "FROM staff s " +
                "LEFT JOIN job_position jp ON s.id_position = jp.id_position " +
                "LEFT JOIN shift sh ON s.id_shift = sh.id_shift " +
                "WHERE s.status = 'INACTIVE' " +
                "ORDER BY s.first_name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                inactivos.add(mapRow(rs));
            }
        }
        return inactivos;
    }

    // 5d. REACTIVAR (vuelve a poner ACTIVE a un empleado inactivo)
    public boolean reactivate(String id) throws SQLException {
        String sql = "UPDATE staff SET status = 'ACTIVE' WHERE id = ?";
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
        String sql = "SELECT * FROM staff WHERE dni = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dni);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Staff s = new Staff();
                    s.setId(rs.getString("id"));
                    s.setFirstName(rs.getString("first_name"));
                    s.setLastName(rs.getString("last_name"));
                    s.setDni(rs.getString("dni"));
                    return s;
                }
            }
        }
        return null;
    }

    private String generateNextId() throws SQLException {
        String sql = "SELECT id FROM staff ORDER BY id DESC LIMIT 1";
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

    // Registra el historial sin arriesgar la operación principal:
    // si staff_history no existe o falla por cualquier motivo, solo lo avisamos
    // por consola, pero NO hacemos fallar el alta/edición/inactivación ya exitosa.
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
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setDni(rs.getString("dni"));
        s.setBirthDate(rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null);
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setStreet(rs.getString("street"));
        s.setAddressNumber(rs.getString("address_number"));
        s.setCity(rs.getString("city"));
        s.setIdPosition(rs.getInt("id_position"));
        int idShiftValue = rs.getInt("id_shift");
        s.setIdShift(rs.wasNull() ? null : idShiftValue);
        s.setStatus(StaffStatus.valueOf(rs.getString("status")));
        s.setHireDate(rs.getDate("hire_date") != null ? rs.getDate("hire_date").toLocalDate() : null);
        s.setShiftStart(rs.getTime("shift_start") != null ? rs.getTime("shift_start").toLocalTime() : null);
        s.setShiftEnd(rs.getTime("shift_end") != null ? rs.getTime("shift_end").toLocalTime() : null);
        s.setSalary(rs.getBigDecimal("salary"));
        s.setPositionName(rs.getString("positionName"));
        s.setShiftName(rs.getString("shiftName"));
        return s;
    }
}