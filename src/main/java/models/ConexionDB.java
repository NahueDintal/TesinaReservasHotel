package models;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

  // Cargar variables desde .env
  private static final Dotenv dotenv = Dotenv.configure()
      .ignoreIfMissing() // Si no encuentra .env, no falla (útil en producción)
      .load();

  private static final String URL = dotenv.get("DB_URL", "jdbc:mysql://localhost:3306/defaultDB");
  private static final String USER = dotenv.get("DB_USER", "root");
  private static final String PASSWORD = dotenv.get("DB_PASSWORD", "");

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
      System.out.println("✅ ¡Conexión exitosa a " + conn.getCatalog() + "!");
    } catch (SQLException e) {
      System.err.println("❌ Error: " + e.getMessage());
    } finally {
      ConexionDB.closeConnection();
    }
  }
}
