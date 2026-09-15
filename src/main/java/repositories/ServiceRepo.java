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
            System.err.println(
                    "Error al obtener los servicios: " + e.getMessage()
            );
        }

        return services;
    }

}


