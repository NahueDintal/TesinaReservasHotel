
package models;

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
        throw new SQLException("Driver de MySQL no encontrado. ¿Agregaste la dependencia?", e);
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
}
