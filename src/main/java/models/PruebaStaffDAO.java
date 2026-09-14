package models;

import repositories.StaffDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;

public class PruebaStaffDAO {

    public static void main(String[] args) {
        StaffDAO staffDAO = new StaffDAO();

        try {
            // 1) Creamos un empleado nuevo (sin id, StaffDAO se lo asigna solo)
            // idPosition=4 (Mantenimiento) e idShift=2 (Tarde) según tus catálogos actuales:
            // confirmá los números reales con SELECT * FROM job_position; y SELECT * FROM shift;
            Staff nuevoEmpleado = new Staff(
                    "Lucas", "Fernández", "35111222",
                    LocalDate.of(1995, 6, 20),
                    "3541 999888", "lucas.fernandez@hoteltemu.com",
                    "Belgrano", "456", "Villa Carlos Paz",
                    4,      // idPosition
                    2,      // idShift (o null si no querés asignarle turno)
                    StaffStatus.ACTIVE,
                    LocalDate.now(),
                    LocalTime.of(15, 0), LocalTime.of(23, 0),
                    new BigDecimal("480000")
            );

            boolean insertado = staffDAO.isInsert(nuevoEmpleado);
            System.out.println("¿Se insertó?: " + insertado + " | id asignado: " + nuevoEmpleado.getId());

            Staff leido = staffDAO.searchById(nuevoEmpleado.getId());
            System.out.println("Leído desde la base: " + leido + " - " + leido.getPositionName());

            List<Staff> todos = staffDAO.listAll();
            System.out.println("Total de empleados: " + todos.size());
            for (Staff s : todos) {
                System.out.println(" - " + s.getFullName() + " (" + s.getPositionName() + ")");
            }

            staffDAO.deactivate(nuevoEmpleado.getId());
            Staff inactivo = staffDAO.searchById(nuevoEmpleado.getId());
            System.out.println("Estado luego de inactivar: " + inactivo.getStatus());

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}