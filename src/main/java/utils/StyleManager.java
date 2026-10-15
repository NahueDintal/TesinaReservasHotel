package utils;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class StyleManager {

    private static final String CSS_PATH = "/css/style.css";

    // 🎯 Ventana padre para centrar los diálogos. Se registra una vez al arrancar.
    private static Window owner;

    public static void setOwner(Window w) {
        owner = w;
    }

    public static Window getOwner() {
        return owner;
    }

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

    /**
     * Configura un Alert para que herede el CSS, se centre sobre la ventana
     * padre (si está registrada) y tenga tamaño controlado.
     * Llamar SIEMPRE después de crear el Alert y antes de showAndWait().
     */
    public static void configureDialog(Alert alert) {
        if (alert == null) return;

        DialogPane pane = alert.getDialogPane();
        applyStyles(pane);

        pane.setPrefWidth(420);
        pane.setMaxWidth(420);
        pane.setMinHeight(160);

        if (owner != null && owner.isShowing()) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        }

        alert.setOnShown(ev -> {
            Stage stage = (Stage) pane.getScene().getWindow();
            if (owner != null && owner.isShowing()) {
                stage.setX(owner.getX() + (owner.getWidth()  - stage.getWidth())  / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            }
        });
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        configureDialog(alert);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}
