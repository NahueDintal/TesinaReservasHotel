package utils;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

public class StyleManager {

    // Ruta del CSS global (ajustá si es diferente)
    private static final String CSS_PATH = "/css/style.css";

    /**
     * Aplica el CSS global a una escena.
     * Uso: StyleManager.applyStyles(scene);
     */
    public static void applyStyles(Scene scene) {
        if (scene == null) return;
        String css = StyleManager.class.getResource(CSS_PATH).toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    /**
     * Aplica el CSS global a un Stage (ventana).
     * Uso: StyleManager.applyStyles(stage);
     */
    public static void applyStyles(Stage stage) {
        if (stage == null || stage.getScene() == null) return;
        applyStyles(stage.getScene());
    }

    /**
     * Aplica el CSS global a un DialogPane (usado en Alertas).
     * Uso: StyleManager.applyStyles(alert.getDialogPane());
     */
    public static void applyStyles(DialogPane dialogPane) {
        if (dialogPane == null) return;
        String css = StyleManager.class.getResource(CSS_PATH).toExternalForm();
        if (!dialogPane.getStylesheets().contains(css)) {
            dialogPane.getStylesheets().add(css);
        }
    }
    /**
     * Crea un Alert de confirmación con el CSS ya aplicado.
     * Uso: boolean confirm = StyleManager.showConfirmation("Título", "Header", "Contenido");
     */
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