package repositories;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .ignoreIfMissing()
            .load();

    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    private static Connection connection = null;

    private ConexionDB() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (URL == null || USER == null || PASSWORD == null) {
                throw new SQLException("Variables de entorno no cargadas. Revisa el archivo .env y la ruta.");
            }
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

    public static void main(String[] args) {
        try (Connection conn = ConexionDB.getConnection()) {
            System.out.println("¡Conexión exitosa a " + conn.getCatalog() + "!");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            ConexionDB.closeConnection();
        }
    }
}
