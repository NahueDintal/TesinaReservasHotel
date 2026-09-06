package utils;

public class Utils {
    /**
     * Limpia y valida un número de teléfono.
     * - Elimina espacios, guiones, paréntesis y puntos.
     * - Valida que tenga entre 7 y 15 dígitos (estándar internacional E.164).
     * - Si el número original comienza con '+', se mantiene en el resultado.
     *
     * @param rawPhone Número de teléfono "sucio" (ej: "+54 351 123-4567")
     * @return Teléfono limpio (ej: "+543511234567") o null si es inválido.
     */
    public static String cleanAndValidatePhone(String rawPhone) {
        // 1. Validar que no sea nulo o esté vacío
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            System.err.println("⚠️ Error: El número de teléfono está vacío.");
            return null;
        }

        // 2. Detectar si tiene prefijo internacional '+'
        String phone = rawPhone.trim();
        boolean hasPlusPrefix = phone.startsWith("+");

        // 3. Limpieza: Remover TODO lo que NO sea dígito (0-9)
        String onlyDigits = phone.replaceAll("\\D", "");

        // 4. Validar longitud (mínimo 7, máximo 15 dígitos)
        int digitCount = onlyDigits.length();
        if (digitCount < 7 || digitCount > 15) {
            System.err.println("⚠️ Error: Cantidad de dígitos inválida (" + digitCount + "). Debe ser entre 7 y 15.");
            return null;
        }

        // 5. Devolver el número limpio (con '+' si lo tenía)
        return hasPlusPrefix ? "+" + onlyDigits : onlyDigits;
    }
}
