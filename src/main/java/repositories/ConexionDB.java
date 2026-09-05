package repositories;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

  private static final Dotenv dotenv = Dotenv.configure()
      .ignoreIfMissing()
      .load();

  private static final String URL = dotenv.get("DB_URL");
  private static final String USER = dotenv.get("DB_USER");
  private static final String PASSWORD = dotenv.get("DB_PASSWORD");

  private ConexionDB() {
  }

  public static Connection getConnection() throws SQLException {

    if (URL == null || URL.isBlank()) {
      throw new SQLException("DB_URL no está configurada.");
    }

    if (USER == null || USER.isBlank()) {
      throw new SQLException("DB_USER no está configurado.");
    }

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");

    } catch (ClassNotFoundException e) {

      throw new SQLException(
          "Driver de MySQL no encontrado.",
          e);
    }

    return DriverManager.getConnection(
        URL,
        USER,
        PASSWORD);
  }

  public static void main(String[] args) {

    try (Connection conn = ConexionDB.getConnection()) {

      System.out.println(
          "✅ ¡Conexión exitosa a "
              + conn.getCatalog()
              + "!");

    } catch (SQLException e) {

      System.err.println(
          "❌ Error: "
              + e.getMessage());
    }
  }
}
