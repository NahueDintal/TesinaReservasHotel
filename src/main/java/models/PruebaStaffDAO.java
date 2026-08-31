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
            Staff nuevoEmpleado = new Staff(
                    "Lucas", "Fernández", "35.111.222",
                    LocalDate.of(1995, 6, 20),
                    "3541 999888", "lucas.fernandez@hoteltemu.com",
                    "Belgrano", "456", "Villa Carlos Paz",
                    4, 3, // idPosition=4 (Mantenimiento), idDepartment=3 (Mantenimiento) según los INSERT del catálogo
                    StaffStatus.ACTIVE,
                    LocalDate.now(),
                    "Tarde", LocalTime.of(15, 0), LocalTime.of(23, 0),
                    new BigDecimal("480000")
            );

            boolean insertado = staffDAO.isInsert(nuevoEmpleado);
            System.out.println("¿Se insertó?: " + insertado + " | id asignado: " + nuevoEmpleado.getId());

            Staff leido = staffDAO.searchById(nuevoEmpleado.getId());
            System.out.println("Leído desde la base: " + leido + " - " + leido.getPositionName() + " / " + leido.getDepartmentName());

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