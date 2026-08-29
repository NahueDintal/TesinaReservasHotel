package repositories;

import models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // 1. LIST ALL (WITH JOINS TO GET NAMES)
    public List<Customer> listAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "dt.name AS documentTypeName, " +
                "co.name AS countryName, " +
                "cs.name AS statusName, " +
                "co2.name AS originName " +
                "FROM Customer c " +
                "LEFT JOIN DocumentType dt ON c.idDocumentType = dt.idDocumentType " +
                "LEFT JOIN Country co ON c.idCountry = co.idCountry " +
                "LEFT JOIN CustomerStatus cs ON c.idCustomerStatus = cs.idCustomerStatus " +
                "LEFT JOIN CustomerOrigin co2 ON c.idCustomerOrigin = co2.idCustomerOrigin " +
                "ORDER BY c.idCustomer DESC";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer c = new Customer();
                // Base fields
                c.setIdCustomer(rs.getInt("idCustomer"));
                c.setName(rs.getString("name"));
                c.setSurname(rs.getString("surname"));
                c.setIdDocumentType(rs.getInt("idDocumentType"));
                c.setDocumentNumber(rs.getString("documentNumber"));
                c.setPhoneNumber(rs.getString("phoneNumber"));
                c.setEmail(rs.getString("email"));
                c.setIdCountry(rs.getInt("idCountry"));
                c.setIdCustomerStatus(rs.getInt("idCustomerStatus"));
                c.setIdCustomerOrigin(rs.getInt("idCustomerOrigin"));

                // Names from JOINs (for display purposes)
                c.setDocumentTypeName(rs.getString("documentTypeName"));
                c.setCountryName(rs.getString("countryName"));
                c.setStatusName(rs.getString("statusName"));
                c.setOriginName(rs.getString("originName"));

                customers.add(c);
            }
        }
        return customers;
    }

    // 2. SEARCH BY ID
    public Customer searchById(int id) throws SQLException {
        String sql = "SELECT c.*, " +
                "dt.name AS documentTypeName, " +
                "co.name AS countryName, " +
                "cs.name AS statusName, " +
                "co2.name AS originName " +
                "FROM Customer c " +
                "LEFT JOIN DocumentType dt ON c.idDocumentType = dt.idDocumentType " +
                "LEFT JOIN Country co ON c.idCountry = co.idCountry " +
                "LEFT JOIN CustomerStatus cs ON c.idCustomerStatus = cs.idCustomerStatus " +
                "LEFT JOIN CustomerOrigin co2 ON c.idCustomerOrigin = co2.idCustomerOrigin " +
                "WHERE c.idCustomer = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setIdCustomer(rs.getInt("idCustomer"));
                    c.setName(rs.getString("name"));
                    c.setSurname(rs.getString("surname"));
                    c.setIdDocumentType(rs.getInt("idDocumentType"));
                    c.setDocumentNumber(rs.getString("documentNumber"));
                    c.setPhoneNumber(rs.getString("phoneNumber"));
                    c.setEmail(rs.getString("email"));
                    c.setIdCountry(rs.getInt("idCountry"));
                    c.setIdCustomerStatus(rs.getInt("idCustomerStatus"));
                    c.setIdCustomerOrigin(rs.getInt("idCustomerOrigin"));
                    c.setDocumentTypeName(rs.getString("documentTypeName"));
                    c.setCountryName(rs.getString("countryName"));
                    c.setStatusName(rs.getString("statusName"));
                    c.setOriginName(rs.getString("originName"));
                    return c;
                }
            }
        }
        return null;
    }

    // 3. INSERT
    public boolean isInsert(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customer (name, surname, idDocumentType, documentNumber, " +
                "phoneNumber, email, idCountry, idCustomerStatus, idCustomerOrigin) " +
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

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
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

    // 4. UPDATE
    public boolean isUpdate(Customer customer) throws SQLException {
        String sql = "UPDATE Customer SET name=?, surname=?, idDocumentType=?, documentNumber=?, " +
                "phoneNumber=?, email=?, idCountry=?, idCustomerStatus=?, idCustomerOrigin=? " +
                "WHERE idCustomer=?";
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

    // 5. DELETE
    public boolean isDelete(int id) throws SQLException {
        String sql = "DELETE FROM Customer WHERE idCustomer = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // 6. FIND BY DOCUMENT NUMBER (for duplicate validation)
    public Customer findByDocumentNumber(String documentNumber) throws SQLException {
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
                    c.setIdDocumentType(rs.getInt("idDocumentType"));
                    c.setDocumentNumber(rs.getString("documentNumber"));
                    c.setPhoneNumber(rs.getString("phoneNumber"));
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

    // 7. validation for duplicated documentation
    public boolean isDuplicatedByDocumentation(String documentNumber, int idDocumentType, int excludeCustomerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customer WHERE documentNumber = ? AND idDocumentType = ?";

        if (excludeCustomerId > 0) {
            sql += " AND idCustomer != ?";
        }

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documentNumber);
            stmt.setInt(2, idDocumentType);

            if (excludeCustomerId > 0) {
                stmt.setInt(3, excludeCustomerId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1); // Aquí obtenemos el COUNT
                    return count > 0; // Si count > 0, existe duplicado
                }
            }
        }
        return false;
    }
    // 8. validation for duplicated phonenumber
    public boolean isDuplicatedByPhone(String phoneNumber, int excludeCustomerId) throws SQLException {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM Customer WHERE phoneNumber = ?";

        if (excludeCustomerId > 0) {
            sql += " AND idCustomer != ?";
        }

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNumber);

            if (excludeCustomerId > 0) {
                stmt.setInt(2, excludeCustomerId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }
    public static void main(String[] args) {
        try {
            System.out.println("=== TIPOS DE DOCUMENTO ===");
            System.out.println(new DocumentTypeDAO().listAll());

            System.out.println("=== PAÍSES ===");
            System.out.println(new CountryDAO().listAll());

            System.out.println("=== ESTADOS ===");
            System.out.println(new CustomerStatusDAO().listAll());

            System.out.println("=== ORÍGENES ===");
            System.out.println(new CustomerOriginDAO().listAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}