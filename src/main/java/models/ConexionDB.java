package main.java.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

  private static final String URL = "jdbc:mysql://localhost:3306/NachoDB";
  private static final String USER = "root";
  private static final String PASSWORD = "12345678";

  private static Connection connection = null;

  private ConexionDB() {
  }

  public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        throw new SQLException("Driver de MySQL no encontrado.", e);
      }
      connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }
    return connection;
  }

  public static void closeConnection() {
    if (connection != null) {
      try {
        connection.close();
        connection = null;
      } catch (SQLException e) {
        System.err.println("Error al cerrar la conexión: " + e.getMessage());
      }
    }
  }

  // Para probar la conexión
  public static void main(String[] args) {
    try (Connection conn = ConexionDB.getConnection()) {
      System.out.println("✅ ¡Conexión exitosa!");
      System.out.println("📌 Base de datos: " + conn.getCatalog());
    } catch (SQLException e) {
      System.err.println("❌ Error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      ConexionDB.closeConnection();
    }
  }
}
