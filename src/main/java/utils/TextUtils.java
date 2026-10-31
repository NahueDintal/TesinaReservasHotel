package utils;

import java.util.Locale;

public final class TextUtils {

    private TextUtils() {
    }

    /**
     * Pone en mayúscula SOLO la primera letra y el resto en minúscula.
     * Ej: "juan" -> "Juan", "PEDRO" -> "Pedro"
     */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT)
                + s.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Pone en mayúscula la primera letra de CADA palabra.
     * Ej: "juan carlos" -> "Juan Carlos", "san jose" -> "San Jose"
     */
    public static String titleCase(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        String[] words = s.trim().toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(w.charAt(0)))
              .append(w.substring(1));
        }
        return sb.toString();
    }
}
