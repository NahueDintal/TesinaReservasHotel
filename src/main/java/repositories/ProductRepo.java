package repositories;

import models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepo {

    public List<Product> getActiveProducts() {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT idProduct, name, description, price, active " +
                "FROM Product " +
                "WHERE active = TRUE " +
                "ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product();

                product.setIdProduct(rs.getInt("idProduct"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setActive(rs.getBoolean("active"));

                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los productos: " + e.getMessage());
        }

        return products;
    }

    public List<Product> getInactiveProducts() {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT idProduct, name, description, price, active " +
                "FROM Product " +
                "WHERE active = FALSE " +
                "ORDER BY name";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product();

                product.setIdProduct(rs.getInt("idProduct"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setActive(rs.getBoolean("active"));

                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener los productos inactivos: " + e.getMessage());
        }

        return products;
    }

    public boolean insert(Product product) {

        String sql = "INSERT INTO Product (name, description, price, active) " +
                "VALUES (?, ?, ?, TRUE)";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Product product) {

        String sql = "UPDATE Product " +
                "SET name = ?, description = ?, price = ? " +
                "WHERE idProduct = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getIdProduct());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean deactivate(int idProduct) {

        String sql = "UPDATE Product " +
                "SET active = FALSE " +
                "WHERE idProduct = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProduct);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean activate(int idProduct) {

        String sql = "UPDATE Product " +
                "SET active = TRUE " +
                "WHERE idProduct = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProduct);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al activar producto: " + e.getMessage());
            return false;
        }
    }

    public boolean existsByName(String name, int excludeId) {

        String sql = "SELECT COUNT(*) " +
                "FROM Product " +
                "WHERE LOWER(name) = LOWER(?) " +
                "AND idProduct <> ?";

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
            System.err.println("Error al verificar nombre de producto: " + e.getMessage());
        }

        return false;
    }

    public List<Product> searchProducts(String searchTerm) {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT idProduct, name, description, price, active " +
                "FROM Product " +
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
                    Product product = new Product();

                    product.setIdProduct(rs.getInt("idProduct"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setActive(rs.getBoolean("active"));

                    products.add(product);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
        }

        return products;
    }
}