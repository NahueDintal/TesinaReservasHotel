package repositories;

import models.Customer;
import utils.TextUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // ============================================================
    // 1. LIST ALL (ACTIVOS)
    // ============================================================
    public List<Customer> listAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "dt.name AS documentTypeName, " +
                "co.name AS countryName, " +
                "pr.name AS provinceName, " +
                "cs.name AS statusName, " +
                "co2.name AS originName " +
                "FROM Customer c " +
                "LEFT JOIN DocumentType dt ON c.idDocumentType = dt.idDocumentType " +
                "LEFT JOIN Country co ON c.idCountry = co.idCountry " +
                "LEFT JOIN Province pr ON c.idProvince = pr.idProvince " +
                "LEFT JOIN CustomerStatus cs ON c.idCustomerStatus = cs.idCustomerStatus " +
                "LEFT JOIN CustomerOrigin co2 ON c.idCustomerOrigin = co2.idCustomerOrigin " +
                "WHERE cs.name = 'active' " +
                "ORDER BY c.idCustomer DESC " +
                "LIMIT 100";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
        }
        return customers;
    }

    // ============================================================
    // 1.b LIST ALL INACTIVOS
    // ============================================================
    public List<Customer> listAllInactive() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "dt.name AS documentTypeName, " +
                "co.name AS countryName, " +
                "pr.name AS provinceName, " +
                "cs.name AS statusName, " +
                "co2.name AS originName " +
                "FROM Customer c " +
                "LEFT JOIN DocumentType dt ON c.idDocumentType = dt.idDocumentType " +
                "LEFT JOIN Country co ON c.idCountry = co.idCountry " +
                "LEFT JOIN Province pr ON c.idProvince = pr.idProvince " +
                "LEFT JOIN CustomerStatus cs ON c.idCustomerStatus = cs.idCustomerStatus " +
                "LEFT JOIN CustomerOrigin co2 ON c.idCustomerOrigin = co2.idCustomerOrigin " +
                "WHERE cs.name = 'inactive' " +
                "ORDER BY c.idCustomer DESC " +
                "LIMIT 100";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
        }
        return customers;
    }

    // ============================================================
    // 2. SEARCH BY ID
    // ============================================================
    public Customer searchById(int id) throws SQLException {
        String sql = "SELECT c.*, " +
                "dt.name AS documentTypeName, " +
                "co.name AS countryName, " +
                "pr.name AS provinceName, " +
                "cs.name AS statusName, " +
                "co2.name AS originName " +
                "FROM Customer c " +
                "LEFT JOIN DocumentType dt ON c.idDocumentType = dt.idDocumentType " +
                "LEFT JOIN Country co ON c.idCountry = co.idCountry " +
                "LEFT JOIN Province pr ON c.idProvince = pr.idProvince " +
                "LEFT JOIN CustomerStatus cs ON c.idCustomerStatus = cs.idCustomerStatus " +
                "LEFT JOIN CustomerOrigin co2 ON c.idCustomerOrigin = co2.idCustomerOrigin " +
                "WHERE c.idCustomer = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCustomer(rs);
                }
            }
        }
        return null;
    }

    // ============================================================
    // 3. INSERT
    // ============================================================
    public boolean isInsert(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customer (name, surname, idDocumentType, documentNumber, " +
                "phoneNumber, email, idCountry, idProvince, idCustomerStatus, idCustomerOrigin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getSurname());
            stmt.setInt(3, customer.getIdDocumentType());
            stmt.setString(4, customer.getDocumentNumber());
            stmt.setString(5, customer.getPhoneNumber());
            stmt.setString(6, customer.getEmail());
            stmt.setInt(7, customer.getIdCountry());

            if (customer.getIdProvince() == 0) {
                stmt.setNull(8, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(8, customer.getIdProvince());
            }

            stmt.setInt(9, customer.getIdCustomerStatus());
            stmt.setInt(10, customer.getIdCustomerOrigin());

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

    // ============================================================
    // 4. UPDATE
    // ============================================================
    public boolean isUpdate(Customer customer) throws SQLException {
        String sql = "UPDATE Customer SET name=?, surname=?, idDocumentType=?, documentNumber=?, " +
                "phoneNumber=?, email=?, idCountry=?, idProvince=?, idCustomerStatus=?, idCustomerOrigin=? " +
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

            if (customer.getIdProvince() == 0) {
                stmt.setNull(8, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(8, customer.getIdProvince());
            }

            stmt.setInt(9, customer.getIdCustomerStatus());
            stmt.setInt(10, customer.getIdCustomerOrigin());
            stmt.setInt(11, customer.getIdCustomer());

            return stmt.executeUpdate() > 0;
        }
    }

    // ============================================================
    // 5. VALIDACIÓN DUPLICADOS
    // ============================================================
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
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // ============================================================
    // 6. COUNT
    // ============================================================
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customer";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // ============================================================
    // 7. SEARCH ACTIVOS
    // ============================================================
    public List<Customer> searchCustomers(String searchTerm) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "dt.name AS documentTypeName, " +
                "co.name AS countryName, " +
                "pr.name AS provinceName, " +
                "cs.name AS statusName, " +
                "co2.name AS originName " +
                "FROM Customer c " +
                "LEFT JOIN DocumentType dt ON c.idDocumentType = dt.idDocumentType " +
                "LEFT JOIN Country co ON c.idCountry = co.idCountry " +
                "LEFT JOIN Province pr ON c.idProvince = pr.idProvince " +
                "LEFT JOIN CustomerStatus cs ON c.idCustomerStatus = cs.idCustomerStatus " +
                "LEFT JOIN CustomerOrigin co2 ON c.idCustomerOrigin = co2.idCustomerOrigin " +
                "WHERE (c.name LIKE ? " +
                "   OR c.surname LIKE ? " +
                "   OR c.documentNumber LIKE ? " +
                "   OR c.phoneNumber LIKE ? " +
                "   OR c.email LIKE ? " +
                "   OR dt.name LIKE ? " +
                "   OR co.name LIKE ? " +
                "   OR pr.name LIKE ? " +
                "   OR co2.name LIKE ?) " +
                "AND cs.name = 'active' " +
                "ORDER BY c.idCustomer DESC " +
                "LIMIT 100";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + searchTerm + "%";
            for (int i = 1; i <= 9; i++) {
                stmt.setString(i, likeTerm);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapCustomer(rs));
                }
            }
        }
        return customers;
    }

    // ============================================================
    // 8. SEARCH INACTIVOS
    // ============================================================
    public List<Customer> searchInactiveCustomers(String searchTerm) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT c.*, " +
                "dt.name AS documentTypeName, " +
                "co.name AS countryName, " +
                "pr.name AS provinceName, " +
                "cs.name AS statusName, " +
                "co2.name AS originName " +
                "FROM Customer c " +
                "LEFT JOIN DocumentType dt ON c.idDocumentType = dt.idDocumentType " +
                "LEFT JOIN Country co ON c.idCountry = co.idCountry " +
                "LEFT JOIN Province pr ON c.idProvince = pr.idProvince " +
                "LEFT JOIN CustomerStatus cs ON c.idCustomerStatus = cs.idCustomerStatus " +
                "LEFT JOIN CustomerOrigin co2 ON c.idCustomerOrigin = co2.idCustomerOrigin " +
                "WHERE (c.name LIKE ? " +
                "   OR c.surname LIKE ? " +
                "   OR c.documentNumber LIKE ? " +
                "   OR c.phoneNumber LIKE ? " +
                "   OR c.email LIKE ? " +
                "   OR dt.name LIKE ? " +
                "   OR co.name LIKE ? " +
                "   OR pr.name LIKE ? " +
                "   OR co2.name LIKE ?) " +
                "AND cs.name = 'inactive' " +
                "ORDER BY c.idCustomer DESC " +
                "LIMIT 100";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String likeTerm = "%" + searchTerm + "%";
            for (int i = 1; i <= 9; i++) {
                stmt.setString(i, likeTerm);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapCustomer(rs));
                }
            }
        }
        return customers;
    }

    // ============================================================
    // HELPER: mapea un ResultSet a Customer aplicando formato
    // ============================================================
    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setIdCustomer(rs.getInt("idCustomer"));
        c.setName(TextUtils.titleCase(rs.getString("name")));
        c.setSurname(TextUtils.titleCase(rs.getString("surname")));
        c.setIdDocumentType(rs.getInt("idDocumentType"));
        c.setDocumentNumber(rs.getString("documentNumber"));
        c.setPhoneNumber(rs.getString("phoneNumber"));
        c.setEmail(rs.getString("email"));
        c.setIdCountry(rs.getInt("idCountry"));
        c.setIdProvince(rs.getInt("idProvince"));
        c.setIdCustomerStatus(rs.getInt("idCustomerStatus"));
        c.setIdCustomerOrigin(rs.getInt("idCustomerOrigin"));

        // 👇 Aquí aplicamos el formateo SOLO a los campos de texto libre
        c.setDocumentTypeName(rs.getString("documentTypeName")); // NO se toca (ej: "DNI", "CE")
        c.setCountryName(TextUtils.titleCase(rs.getString("countryName")));
        c.setProvinceName(TextUtils.titleCase(rs.getString("provinceName")));
        c.setStatusName(rs.getString("statusName"));             // NO se toca (es "active"/"inactive")
        c.setOriginName(TextUtils.titleCase(rs.getString("originName")));

        return c;
    }
}
