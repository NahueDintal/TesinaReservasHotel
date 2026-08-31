package repositories;

import models.Staff;
import models.StaffStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    // 1. LIST ALL (WITH JOINS TO GET NAMES)
    public List<Staff> listAll() throws SQLException {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT s.*, " +
                "jp.name AS positionName, " +
                "d.name AS departmentName " +
                "FROM staff s " +
                "LEFT JOIN job_position jp ON s.id_position = jp.id_position " +
                "LEFT JOIN department d ON s.id_department = d.id_department " +
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
        String sql = "SELECT s.*, " +
                "jp.name AS positionName, " +
                "d.name AS departmentName " +
                "FROM staff s " +
                "LEFT JOIN job_position jp ON s.id_position = jp.id_position " +
                "LEFT JOIN department d ON s.id_department = d.id_department " +
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
        // Como el id no es autoincremental, lo generamos acá si todavía no tiene uno
        if (staff.getId() == null || staff.getId().isEmpty()) {
            staff.setId(generateNextId());
        }

        String sql = "INSERT INTO staff (id, first_name, last_name, dni, birth_date, phone, email, " +
                "street, address_number, city, id_position, id_department, status, hire_date, " +
                "shift_name, shift_start, shift_end, salary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            stmt.setInt(12, staff.getIdDepartment());
            stmt.setString(13, staff.getStatus().name());
            stmt.setDate(14, staff.getHireDate() != null ? Date.valueOf(staff.getHireDate()) : null);
            stmt.setString(15, staff.getShiftName());
            stmt.setTime(16, staff.getShiftStart() != null ? Time.valueOf(staff.getShiftStart()) : null);
            stmt.setTime(17, staff.getShiftEnd() != null ? Time.valueOf(staff.getShiftEnd()) : null);
            stmt.setBigDecimal(18, staff.getSalary());

            return stmt.executeUpdate() > 0;
        }
    }

    // 4. UPDATE
    public boolean isUpdate(Staff staff) throws SQLException {
        String sql = "UPDATE staff SET first_name=?, last_name=?, phone=?, email=?, " +
                "street=?, address_number=?, city=?, id_position=?, id_department=?, " +
                "status=?, shift_name=?, shift_start=?, shift_end=?, salary=? " +
                "WHERE id=?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staff.getFirstName());
            stmt.setString(2, staff.getLastName());
            stmt.setString(3, staff.getPhone());
            stmt.setString(4, staff.getEmail());
            stmt.setString(5, staff.getStreet());
            stmt.setString(6, staff.getAddressNumber());
            stmt.setString(7, staff.getCity());
            stmt.setInt(8, staff.getIdPosition());
            stmt.setInt(9, staff.getIdDepartment());
            stmt.setString(10, staff.getStatus().name());
            stmt.setString(11, staff.getShiftName());
            stmt.setTime(12, staff.getShiftStart() != null ? Time.valueOf(staff.getShiftStart()) : null);
            stmt.setTime(13, staff.getShiftEnd() != null ? Time.valueOf(staff.getShiftEnd()) : null);
            stmt.setBigDecimal(14, staff.getSalary());
            stmt.setString(15, staff.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 5. DELETE (borrado físico real)
    public boolean isDelete(String id) throws SQLException {
        String sql = "DELETE FROM staff WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // 5b. INACTIVAR (botón "Inactivar Personal" — no borra, solo cambia el estado)
    public boolean deactivate(String id) throws SQLException {
        String sql = "UPDATE staff SET status = 'INACTIVE' WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // 6. FIND BY DNI (para validar duplicados antes de insertar, igual que findByDocumentNumber)
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

    // Genera el siguiente id tipo "001", "002"... (la tabla no tiene autoincremental)
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

    // Convierte una fila del ResultSet en un objeto Staff (incluye los nombres del JOIN)
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
        s.setIdDepartment(rs.getInt("id_department"));
        s.setStatus(StaffStatus.valueOf(rs.getString("status")));
        s.setHireDate(rs.getDate("hire_date") != null ? rs.getDate("hire_date").toLocalDate() : null);
        s.setShiftName(rs.getString("shift_name"));
        s.setShiftStart(rs.getTime("shift_start") != null ? rs.getTime("shift_start").toLocalTime() : null);
        s.setShiftEnd(rs.getTime("shift_end") != null ? rs.getTime("shift_end").toLocalTime() : null);
        s.setSalary(rs.getBigDecimal("salary"));
        s.setPositionName(rs.getString("positionName"));
        s.setDepartmentName(rs.getString("departmentName"));
        return s;
    }
}