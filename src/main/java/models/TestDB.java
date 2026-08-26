package models; // o el paquete que uses

import models.Room;
import repositories.RoomDAO;

import java.util.List;

public class TestDB {
  public static void main(String[] args) {
    RoomDAO roomDAO = new RoomDAO();

    System.out.println("=== PRUEBA DE CONEXIÓN Y ABM ===");

    // 1. Listar todas las habitaciones
    System.out.println("\n--- Listar todas las habitaciones ---");
    try {
      List<Room> rooms = roomDAO.listAll();
      if (rooms.isEmpty()) {
        System.out.println("No hay habitaciones en la base de datos.");
      } else {
        for (Room r : rooms) {
          System.out.println("N°: " + r.getNumber() +
              " | Piso: " + r.getFloor() +
              " | Tipo: " + r.getType() +
              " | Capacidad: " + r.getCapacity() +
              " | Vista: " + r.getView() +
              " | Disponible: " + (r.getIsAvailable() ? "Sí" : "No") +
              " | Fuera de servicio: " + (r.getOutOfService() ? "Sí" : "No") +
              " | Precio: $" + r.getPrice());
        }
      }
    } catch (Exception e) {
      System.err.println("Error al listar: " + e.getMessage());
    }

    // 2. Insertar una nueva habitación
    System.out.println("\n--- Insertar una nueva habitación ---");
    Room nueva = new Room(
        "401", "4", "suite", "2", "mar", "wifi, tv, jacuzzi", "200.00", "Suite de lujo");
    try {
      roomDAO.insert(nueva);
      System.out.println("Habitación insertada correctamente: " + nueva.getNumber());
    } catch (Exception e) {
      System.err.println("Error al insertar: " + e.getMessage());
    }

    // 3. Buscar por número
    System.out.println("\n--- Buscar habitación por número 401 ---");
    try {
      Room encontrada = roomDAO.searchByNumber(401);
      if (encontrada != null) {
        System.out.println("Habitación encontrada: " + encontrada.getNumber() +
            " - " + encontrada.getType() + " - $" + encontrada.getPrice());
      } else {
        System.out.println("No se encontró la habitación 401.");
      }
    } catch (Exception e) {
      System.err.println("Error al buscar: " + e.getMessage());
    }

    // 4. Actualizar una habitación
    System.out.println("\n--- Actualizar habitación 401 ---");
    try {
      Room actualizar = roomDAO.searchByNumber(401);
      if (actualizar != null) {
        // Cambiamos el precio y la vista
        actualizar.setPrice("250.00");
        actualizar.setView("ciudad");
        actualizar.setOutOfService(true); // la ponemos fuera de servicio para probar
        roomDAO.update(actualizar);
        System.out.println("Habitación actualizada. Nuevo precio: $" + actualizar.getPrice() +
            ", Vista: " + actualizar.getView() +
            ", Fuera de servicio: " + (actualizar.getOutOfService() ? "Sí" : "No"));
      } else {
        System.out.println("La habitación 401 no existe para actualizar.");
      }
    } catch (Exception e) {
      System.err.println("Error al actualizar: " + e.getMessage());
    }

    // 5. Verificar que la habitación 401 está fuera de servicio y no disponible
    System.out.println("\n--- Verificar disponibilidad de 401 ---");
    try {
      Room r401 = roomDAO.searchByNumber(401);
      if (r401 != null) {
        System.out.println("Disponible: " + (r401.getIsAvailable() ? "Sí" : "No"));
        System.out.println("Fuera de servicio: " + (r401.getOutOfService() ? "Sí" : "No"));
      }
    } catch (Exception e) {
      System.err.println("Error al verificar: " + e.getMessage());
    }

    // 6. Eliminar la habitación 401
    System.out.println("\n--- Eliminar habitación 401 ---");
    try {
      roomDAO.delete(401);
      System.out.println("Habitación eliminada.");
    } catch (Exception e) {
      System.err.println("Error al eliminar: " + e.getMessage());
    }

    // 7. Listar nuevamente para confirmar
    System.out.println("\n--- Listar después de las operaciones ---");
    try {
      List<Room> rooms = roomDAO.listAll();
      for (Room r : rooms) {
        System.out.println("N°: " + r.getNumber() + " | Tipo: " + r.getType() + " | Precio: $" + r.getPrice());
      }
    } catch (Exception e) {
      System.err.println("Error al listar: " + e.getMessage());
    }

    System.out.println("\n=== FIN DE PRUEBAS ===");
  }
}
