package utils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Gestor central de estilos y tema (claro/oscuro).
 * - Aplica el CSS de la app a Scenes, Stages y DialogPanes.
 * - Recuerda el tema elegido entre ejecuciones.
 * - Provee helpers para Alertas/confirmaciones.
 */
public final class StyleManager {

  private static final String DARK_CLASS = "dark-theme";
  private static final String THEME_KEY = "darkTheme";
  private static final String CSS_PATH = "/css/style.css";

  private static final Preferences PREFS = Preferences.userNodeForPackage(StyleManager.class);

  private static boolean dark = PREFS.getBoolean(THEME_KEY, false);
  private static Window owner = null;

  private StyleManager() {
  }

  // ==========================================================
  // TEMA CLARO / OSCURO
  // ==========================================================

  public static boolean isDark() {
    return dark;
  }

  public static void setDark(boolean value) {
    dark = value;
    PREFS.putBoolean(THEME_KEY, value);
  }

  public static void toggle() {
    setDark(!dark);
  }

  /** Aplica el tema actual al Parent indicado (agrega/quita .dark-theme). */
  public static void applyTheme(Parent root) {
    if (root == null)
      return;
    if (dark) {
      if (!root.getStyleClass().contains(DARK_CLASS)) {
        root.getStyleClass().add(DARK_CLASS);
      }
    } else {
      root.getStyleClass().remove(DARK_CLASS);
    }
  }

  // Alias por si en algún lado quedó la llamada `applyTo`
  public static void applyTo(Parent root) {
    applyTheme(root);
  }

  // ==========================================================
  // OWNER (ventana principal para modales)
  // ==========================================================

  public static Window getOwner() {
    return owner;
  }

  public static void setOwner(Window w) {
    owner = w;
  }

  public static void setOwner(Stage s) {
    owner = s;
  }

  // ==========================================================
  // APLICAR ESTILOS A SCENE / STAGE / DIALOGPANE
  // ==========================================================

  public static void applyStyles(Scene scene) {
    if (scene == null)
      return;
    ensureStylesheet(scene);
    applyTheme(scene.getRoot());
  }

  public static void applyStyles(Stage stage) {
    if (stage == null || stage.getScene() == null)
      return;
    applyStyles(stage.getScene());
  }

  public static void applyStyles(DialogPane dialogPane) {
    if (dialogPane == null)
      return;

    Scene scene = dialogPane.getScene();
    if (scene != null) {
      ensureStylesheet(scene);
    }
    applyTheme(dialogPane);
  }

  /** Alias por compatibilidad: algunos controllers llaman `applyTo(Scene)`. */
  public static void applyTo(Scene scene) {
    applyStyles(scene);
  }

  /** Alias por compatibilidad con nodos arbitrarios. */
  public static void applyTo(javafx.scene.Node node) {
    if (node == null || node.getScene() == null)
      return;
    applyStyles(node.getScene());
  }

  /** Agrega el CSS a la Scene solo si no estaba ya (evita duplicados). */
  private static void ensureStylesheet(Scene scene) {
    try {
      java.net.URL url = StyleManager.class.getResource(CSS_PATH);
      if (url == null)
        return;
      String css = url.toExternalForm();
      if (!scene.getStylesheets().contains(css)) {
        scene.getStylesheets().add(css);
      }
    } catch (Exception ignored) {
      // Si no encuentra el CSS, no rompemos la app
    }
  }

  // ==========================================================
  // DIÁLOGOS Y CONFIRMACIONES
  // ==========================================================

  /**
   * Aplica los estilos a un Alert y le pone una clase extra
   * (error-alert / warning-alert / information-alert) para que
   * el CSS pueda pintarle el header según el tipo.
   */
  public static void configureDialog(Alert alert) {
    if (alert == null)
      return;

    applyStyles(alert.getDialogPane());

    DialogPane pane = alert.getDialogPane();
    pane.getStyleClass().removeAll(
        "error-alert", "warning-alert", "information-alert");

    switch (alert.getAlertType()) {
      case ERROR -> pane.getStyleClass().add("error-alert");
      case WARNING -> pane.getStyleClass().add("warning-alert");
      default -> pane.getStyleClass().add("information-alert");
    }
  }

  /**
   * Muestra un Alert de confirmación con OK / Cancelar
   * y devuelve true si el usuario confirma con OK.
   */
  public static boolean showConfirmation(String title,
      String header,
      String content) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);

    if (owner != null) {
      try {
        alert.initOwner(owner);
      } catch (Exception ignored) {
      }
    }
    configureDialog(alert);

    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
