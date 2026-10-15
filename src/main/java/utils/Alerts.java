package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alerts {

    public static void info(String title, String msg) {
        show(Alert.AlertType.INFORMATION, title, null, msg);
    }

    public static void info(String title, String header, String msg) {
        show(Alert.AlertType.INFORMATION, title, header, msg);
    }

    public static void error(String title, String msg) {
        show(Alert.AlertType.ERROR, title, null, msg);
    }

    public static void error(String title, String header, String msg) {
        show(Alert.AlertType.ERROR, title, header, msg);
    }

    public static void warn(String title, String msg) {
        show(Alert.AlertType.WARNING, title, null, msg);
    }

    public static void warn(String title, String header, String msg) {
        show(Alert.AlertType.WARNING, title, header, msg);
    }

    public static boolean confirm(String title, String header, String msg) {
        return StyleManager.showConfirmation(title, header, msg);
    }

    private static void show(Alert.AlertType type, String title, String header, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(msg);
        StyleManager.configureDialog(a);
        a.showAndWait();
    }
}
