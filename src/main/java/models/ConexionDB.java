package models;  // Ajustá si tu paquete es diferente

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase singleton para manejar la conexión a la base de datos MySQL.
 * Usa el patrón Singleton para tener una única instancia de conexión.
 */
public class ConexionDB {

    // 1. Datos de conexión (CAMBIÁ ESTOS POR LOS TUYOS)
    private static final String URL = "jdbc:mysql://localhost:3306/NachoDB";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";

    // 2. Instancia única de la conexión (patrón Singleton)
    private static Connection connection = null;

    // 3. Constructor privado para evitar instanciar desde afuera
    private ConexionDB() {}

    /**
     * Método público estático para obtener la conexión.
     * Si no existe, la crea; si ya existe, la devuelve.
     * @return Connection activa a la base de datos
     * @throws SQLException si falla la conexión
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Cargar el driver (opcional en versiones modernas, pero por las dudas)
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver de MySQL no encontrado. ¿Agregaste la dependencia?", e);
            }
            // Establecer la conexión
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * Método para cerrar la conexión manualmente (cuando la app termina)
     */
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

    //ejecutar para probar conexion
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