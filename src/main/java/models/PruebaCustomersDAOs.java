package models;

import java.sql.SQLException;
import java.util.Map;

public class PruebaCustomersDAOs {
    public static void main(String[] args) {
        try {
            // Probar DocumentType
            DocumentTypeDAO docDAO = new DocumentTypeDAO();
            Map<Integer, String> tipos = docDAO.listAll();
            System.out.println("=== TIPOS DE DOCUMENTO ===");
            for (Map.Entry<Integer, String> entry : tipos.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }

            // Probar Country
            CountryDAO paisDAO = new CountryDAO();
            Map<Integer, String> paises = paisDAO.listAll();
            System.out.println("\n=== PAÍSES ===");
            for (Map.Entry<Integer, String> entry : paises.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }

            // Probar CustomerStatus
            CustomerStatusDAO statusDAO = new CustomerStatusDAO();
            Map<Integer, String> estados = statusDAO.listAll();
            System.out.println("\n=== ESTADOS ===");
            for (Map.Entry<Integer, String> entry : estados.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }

            // Probar CustomerOrigin
            CustomerOriginDAO origenDAO = new CustomerOriginDAO();
            Map<Integer, String> origenes = origenDAO.listAll();
            System.out.println("\n=== ORÍGENES ===");
            for (Map.Entry<Integer, String> entry : origenes.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
