package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> listAll() throws SQLException {
        List<Customer> customer = new ArrayList<>();
        String sql = "SELECT * FROM Customer";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer c = new Customer();
                c.setIdCustomer(rs.getInt("idCustomer"));
                c.setName(rs.getString("name"));
                c.setSurname(rs.getString("surname"));
                c.setIdDocumentType(rs.getInt("idDocumentTye"));
                c.setDocumentNumber(rs.getString("documentNumber"));
                c.setPhoneNumber(rs.getString("phone"));
                c.setEmail(rs.getString("email"));
                c.setIdCountry(rs.getInt("idCountry"));
                c.setIdCustomerStatus(rs.getInt("idCustomerStatus"));
                c.setIdCustomerOrigin(rs.getInt("idCustomerOrigin"));
                customer.add(c);
            }
        }
        return customer;
    }

    public Customer searchById(int idCustomer) throws SQLException {
        String sql = "SELECT * FROM customer WHERE idCustomer = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCustomer);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setIdCustomer(rs.getInt("idCustomer"));
                    c.setName(rs.getString("name"));
                    c.setSurname(rs.getString("surname"));
                    c.setIdDocumentType(rs.getInt("idDocumentTye"));
                    c.setDocumentNumber(rs.getString("documentNumber"));
                    c.setPhoneNumber(rs.getString("phone"));
                    c.setEmail(rs.getString("email"));
                    c.setIdCountry(rs.getInt("idCountry"));
                    c.setIdCustomerStatus(rs.getInt("idCustomerStatus"));
                    c.setIdCustomerOrigin(rs.getInt("idCustomerOrigin"));
                    return c;
                }
            }
        }
        return null;  // No encontrado
    }

    public boolean isInserted(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customer (name, surname, idDocumentType, " +
                "DocumentNumber, phoneNumber, email, idCountry, idCustomerStatus, idCustomerOrigin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {


            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getSurname());
            stmt.setInt(3, customer.getIdDocumentType());
            stmt.setString(4, customer.getDocumentNumber());
            stmt.setString(5, customer.getPhoneNumber());
            stmt.setString(6, customer.getEmail());
            stmt.setInt(7, customer.getIdCountry());
            stmt.setInt(8, customer.getIdCustomerStatus());
            stmt.setInt(9, customer.getIdCustomerOrigin());


            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                // Recuperar el ID generado automáticamente
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setIdCustomer(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean isUpdated(Customer customer) throws SQLException {
        String sql = "UPDATE Customer SET name=?, surname=?,idDocumentType , documentNumber=?, phoneNumber=?, " +
                "email=?, idCountry=?, idCustomerStatus=?, idCustomerOrigin=? WHERE idCustomer=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getSurname());
            stmt.setInt(3, customer.getIdDocumentType());
            stmt.setString(4, customer.getDocumentNumber());
            stmt.setString(5, customer.getPhoneNumber());
            stmt.setString(6, customer.getEmail());
            stmt.setInt(7, customer.getIdCountry());
            stmt.setInt(8, customer.getIdCustomerStatus());
            stmt.setInt(9, customer.getIdCustomerOrigin());
            stmt.setInt(10, customer.getIdCustomer());

            return stmt.executeUpdate() > 0;
        }
    }


    //revisar si es necesario un delete
    public boolean isDeleted(int idCustomer) throws SQLException {
        String sql = "DELETE FROM Customer WHERE idCustomer = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCustomer);
            return stmt.executeUpdate() > 0;
        }
    }

    // 6. BUSCAR POR Documento (para validar duplicados)
    public Customer searchByDocumentNumber(String documentNumber) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE documentNumber = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setIdCustomer(rs.getInt("idCustomer"));
                    c.setName(rs.getString("name"));
                    c.setSurname(rs.getString("surname"));
                    c.setIdDocumentType(rs.getInt("idDocumentTye"));
                    c.setDocumentNumber(rs.getString("documentNumber"));
                    c.setPhoneNumber(rs.getString("phone"));
                    c.setEmail(rs.getString("email"));
                    c.setIdCountry(rs.getInt("idCountry"));
                    c.setIdCustomerStatus(rs.getInt("idCustomerStatus"));
                    c.setIdCustomerOrigin(rs.getInt("idCustomerOrigin"));
                    return c;
                }
            }
        }
        return null;
    }
    public static void main(String[] args) {
        CustomerDAO dao = new CustomerDAO();

        try {
            // Probar LISTAR
            System.out.println("=== LISTAR TODOS ===");
            for (Customer c : dao.listAll()) {
                System.out.println(c.getIdCustomer() + " - " + c.getName() + " " + c.getSurname() );
            }

            // Probar INSERTAR
            Customer nuevo = new Customer(0, "Juan Bautista", "Pérez", 0, "20345678","3541265986", "juan@mail.com", 0,0,0);
            if (dao.isInserted(nuevo)) {
                System.out.println("✅ Insertado con ID: " + nuevo.getIdCustomer());
            }

            // Probar BUSCAR POR ID
            Customer found = dao.searchById(1);
            if (found != null) {
                System.out.println("✅ Encontrado: " + found.getName());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}