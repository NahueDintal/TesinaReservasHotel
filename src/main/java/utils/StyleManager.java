package utils;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

public class StyleManager {

    private static final String CSS_PATH = "/css/style.css";

    public static void applyStyles(Scene scene) {
        if (scene == null) return;
        String css = StyleManager.class.getResource(CSS_PATH).toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    public static void applyStyles(Stage stage) {
        if (stage == null || stage.getScene() == null) return;
        applyStyles(stage.getScene());
    }

    public static void applyStyles(DialogPane dialogPane) {
        if (dialogPane == null) return;
        String css = StyleManager.class.getResource(CSS_PATH).toExternalForm();
        if (!dialogPane.getStylesheets().contains(css)) {
            dialogPane.getStylesheets().add(css);
        }
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        applyStyles(alert.getDialogPane());

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}