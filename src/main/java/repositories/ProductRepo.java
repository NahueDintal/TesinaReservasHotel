package repositories;

import models.Product;

import java.math.BigDecimal;
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
            System.err.println(
                    "Error al obtener los productos: " + e.getMessage()
            );
        }

        return products;
    }

    public Product getProductById(int idProduct) {

        String sql = "SELECT idProduct, name, description, price, active " +
                "FROM Product " +
                "WHERE idProduct = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProduct);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    Product product = new Product();

                    product.setIdProduct(rs.getInt("idProduct"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setActive(rs.getBoolean("active"));

                    return product;
                }
            }

        } catch (SQLException e) {
            System.err.println(
                    "Error al obtener el producto: " + e.getMessage()
            );
        }

        return null;
    }
}
