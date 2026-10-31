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
 * - Separa la estructura (base.css) de los colores (light-theme.css /
 * dark-theme.css).
 * - Recuerda el tema elegido entre ejecuciones.
 */
public final class StyleManager {

  private static final String DARK_CLASS = "dark-theme";
  private static final String THEME_KEY = "darkTheme";

  // Nuevas rutas de los archivos CSS
  private static final String CSS_BASE = "/css/base.css";
  private static final String CSS_LIGHT = "/css/light-theme.css";
  private static final String CSS_DARK = "/css/dark-theme.css";

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

    // Al cambiar el tema, actualizamos TODAS las ventanas abiertas automáticamente
    refreshAllWindows();
  }

  public static void toggle() {
    setDark(!dark);
  }

  /** Aplica la clase dark-theme al root si es necesario. */
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
    updateStylesheet(scene);
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
      updateStylesheet(scene);
    }
    applyTheme(dialogPane);
  }

  public static void applyTo(Scene scene) {
    applyStyles(scene);
  }

  public static void applyTo(javafx.scene.Node node) {
    if (node == null || node.getScene() == null)
      return;
    applyStyles(node.getScene());
  }

  /**
   * Actualiza las hojas de estilo de una escena.
   * Remueve las anteriores y agrega la base + el tema actual.
   */
  private static void updateStylesheet(Scene scene) {
    try {
      String baseUrl = getCssResource(CSS_BASE);
      String lightUrl = getCssResource(CSS_LIGHT);
      String darkUrl = getCssResource(CSS_DARK);

      // 1. Limpiamos por si ya estaban cargados (para evitar duplicados y permitir el
      // cambio)
      scene.getStylesheets().removeAll(baseUrl, lightUrl, darkUrl);

      // 2. Agregamos el CSS base (estructura)
      if (baseUrl != null)
        scene.getStylesheets().add(baseUrl);

      // 3. Agregamos el CSS del tema correspondiente
      if (dark) {
        if (darkUrl != null)
          scene.getStylesheets().add(darkUrl);
      } else {
        if (lightUrl != null)
          scene.getStylesheets().add(lightUrl);
      }
    } catch (Exception e) {
      System.err.println("Error al cargar los estilos: " + e.getMessage());
    }
  }

  private static String getCssResource(String path) {
    java.net.URL url = StyleManager.class.getResource(path);
    return url != null ? url.toExternalForm() : null;
  }

  /** Itera sobre todas las ventanas abiertas y les actualiza el CSS. */
  private static void refreshAllWindows() {
    for (Window window : Window.getWindows()) {
      if (window.getScene() != null) {
        updateStylesheet(window.getScene());
        applyTheme(window.getScene().getRoot());
      }
    }
  }

  // ==========================================================
  // DIÁLOGOS Y CONFIRMACIONES
  // ==========================================================

  public static void configureDialog(Alert alert) {
    if (alert == null)
      return;

    applyStyles(alert.getDialogPane());

    DialogPane pane = alert.getDialogPane();
    pane.getStyleClass().removeAll("error-alert", "warning-alert", "information-alert");

    switch (alert.getAlertType()) {
      case ERROR -> pane.getStyleClass().add("error-alert");
      case WARNING -> pane.getStyleClass().add("warning-alert");
      default -> pane.getStyleClass().add("information-alert");
    }
  }

  public static boolean showConfirmation(String title, String header, String content) {
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
