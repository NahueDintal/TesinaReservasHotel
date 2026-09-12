package utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // ========== PATRONES ==========
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]+(?:[ '-][a-zA-ZáéíóúÁÉÍÓÚñÑüÜ]+)*$");

    private static final Pattern DNI_PATTERN =
            Pattern.compile("^\\d{7,8}$");

    private static final Pattern PASSPORT_PATTERN =
            Pattern.compile("^[A-Za-z]{3}\\d{6}$");

    private static final Pattern FOREIGN_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9]{6,12}$");

    private static final Pattern DRIVER_LICENSE_PATTERN =
            Pattern.compile("^[A-Za-z0-9]{8,12}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9\\s\\-()]{7,20}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // ========== VALIDACIONES ==========

    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 50) return false;
        return NAME_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidDocument(String document, String documentType) {
        if (document == null || document.trim().isEmpty()) return false;
        String doc = document.trim();

        switch (documentType) {
            case "dni":
                return DNI_PATTERN.matcher(doc).matches();
            case "pasaporte":
                return PASSPORT_PATTERN.matcher(doc).matches();
            case "cedula de identidad":
                return DNI_PATTERN.matcher(doc).matches();
            //case "Cédula Extranjera":
                //return FOREIGN_ID_PATTERN.matcher(doc).matches();
            //case "Driver License":
                //return DRIVER_LICENSE_PATTERN.matcher(doc).matches();
            default:
                return false;
        }
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true; // Opcional
        String cleaned = phone.replaceAll("\\D", "");
        return cleaned.length() >= 7 && cleaned.length() <= 15;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true; // Opcional
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // ========== MENSAJES DE ERROR ==========

    public static String getNameError() {
        return "El nombre debe tener entre 2 y 50 letras, sin números ni símbolos.";
    }

    public static String getDocumentError(String documentType) {
        switch (documentType) {
            case "DNI":
                return "El DNI debe tener 7 u 8 dígitos numéricos.";
            case "Pasaporte":
                return "El Pasaporte debe tener 3 letras seguidas de 6 números (ej: ABC123456).";
            case "Cédula":
                return "La Cédula debe tener 7 u 8 dígitos numéricos.";
            case "Cédula Extranjera":
                return "La Cédula Extranjera debe tener entre 6 y 12 caracteres alfanuméricos.";
            case "Driver License":
                return "La Licencia de Conducir debe tener entre 8 y 12 caracteres alfanuméricos.";
            default:
                return "Formato de documento inválido.";
        }
    }

    public static String getPhoneError() {
        return "El teléfono debe tener entre 7 y 15 dígitos.";
    }

    public static String getEmailError() {
        return "El email debe tener un formato válido (ej: usuario@dominio.com).";
    }
}