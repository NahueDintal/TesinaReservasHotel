package repositories;

import models.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepo {

    public List<Service> getActiveServices() {
        List<Service> services = new ArrayList<>();

        String sql = "SELECT idService, name, description, price, active " +
                "FROM Service " +
                "WHERE active = TRUE " +
                "ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Service service = new Service();

                service.setIdService(rs.getInt("idService"));
                service.setName(rs.getString("name"));
                service.setDescription(rs.getString("description"));
                service.setPrice(rs.getBigDecimal("price"));
                service.setActive(rs.getBoolean("active"));

                services.add(service);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los servicios: " + e.getMessage());
        }

        return services;
    }

    public List<Service> getInactiveServices() {
        List<Service> services = new ArrayList<>();

        String sql = "SELECT idService, name, description, price, active " +
                "FROM Service " +
                "WHERE active = FALSE " +
                "ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Service service = new Service();

                service.setIdService(rs.getInt("idService"));
                service.setName(rs.getString("name"));
                service.setDescription(rs.getString("description"));
                service.setPrice(rs.getBigDecimal("price"));
                service.setActive(rs.getBoolean("active"));

                services.add(service);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los servicios inactivos: " + e.getMessage());
        }

        return services;
    }

    public boolean insert(Service service) {

        String sql = "INSERT INTO Service (name, description, price, active) " +
                "VALUES (?, ?, ?, TRUE)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service.getName());
            stmt.setString(2, service.getDescription());
            stmt.setBigDecimal(3, service.getPrice());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar servicio: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Service service) {

        String sql = "UPDATE Service " +
                "SET name = ?, description = ?, price = ? " +
                "WHERE idService = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service.getName());
            stmt.setString(2, service.getDescription());
            stmt.setBigDecimal(3, service.getPrice());
            stmt.setInt(4, service.getIdService());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar servicio: " + e.getMessage());
            return false;
        }
    }

    public boolean deactivate(int idService) {

        String sql = "UPDATE Service " +
                "SET active = FALSE " +
                "WHERE idService = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idService);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar servicio: " + e.getMessage());
            return false;
        }
    }

    public boolean activate(int idService) {

        String sql = "UPDATE Service " +
                "SET active = TRUE " +
                "WHERE idService = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idService);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al activar servicio: " + e.getMessage());
            return false;
        }
    }

    public boolean existsByName(String name, int excludeId) {

        String sql = "SELECT COUNT(*) " +
                "FROM Service " +
                "WHERE LOWER(name) = LOWER(?) " +
                "AND idService <> ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, excludeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar nombre de servicio: " + e.getMessage());
        }

        return false;
    }

    public List<Service> searchServices(String searchTerm) {

        List<Service> services = new ArrayList<>();

        String sql = "SELECT idService, name, description, price, active " +
                "FROM Service " +
                "WHERE active = TRUE " +
                "AND (LOWER(name) LIKE LOWER(?) " +
                "OR LOWER(description) LIKE LOWER(?)) " +
                "ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String search = "%" + searchTerm + "%";

            stmt.setString(1, search);
            stmt.setString(2, search);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Service service = new Service();

                    service.setIdService(rs.getInt("idService"));
                    service.setName(rs.getString("name"));
                    service.setDescription(rs.getString("description"));
                    service.setPrice(rs.getBigDecimal("price"));
                    service.setActive(rs.getBoolean("active"));

                    services.add(service);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar servicios: " + e.getMessage());
        }

        return services;
    }
}
