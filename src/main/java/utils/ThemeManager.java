package utils;

import javafx.scene.Parent;

/**
 * Alias de compatibilidad de StyleManager.
 * @deprecated usar {@link StyleManager} directamente.
 */
@Deprecated
public final class ThemeManager {

    private ThemeManager() { }

    public static boolean isDark() {
        return StyleManager.isDark();
    }

    public static void setDark(boolean value) {
        StyleManager.setDark(value);
    }

    public static void toggle() {
        StyleManager.toggle();
    }

    public static void applyTo(Parent root) {
        StyleManager.applyTo(root);
    }

    public static void applyTo(javafx.scene.Scene scene) {
        StyleManager.applyTo(scene);
    }

    public static void applyTo(javafx.scene.Node node) {
        StyleManager.applyTo(node);
    }
}
