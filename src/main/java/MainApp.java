import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.StyleManager;

public class MainApp extends Application {

  // Variable para mantener el nivel de zoom actual (1.0 = 100%)
  private double escalaActual = 1.0;

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
    BorderPane root = loader.load();
    Scene scene = new Scene(root);
    StyleManager.applyStyles(scene);

    // ========================================================
    // LÓGICA DE ZOOM RECURSIVO (Accesibilidad: Ctrl + / Ctrl -)
    // ========================================================
    scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.isControlDown()) {

        // Ctrl + "+" (Agrandar)
        if (event.getCode() == KeyCode.ADD ||
            event.getCode() == KeyCode.PLUS ||
            event.getCode() == KeyCode.EQUALS) {

          escalaActual += 0.1; // Sube un 10%
          if (escalaActual > 2.5)
            escalaActual = 2.5; // Límite máximo (250%)
          actualizarFuentes(scene.getRoot(), escalaActual);
          event.consume();
        }
        // Ctrl + "-" (Achicar)
        else if (event.getCode() == KeyCode.SUBTRACT ||
            event.getCode() == KeyCode.MINUS) {

          escalaActual -= 0.1; // Baja un 10%
          if (escalaActual < 0.7)
            escalaActual = 0.7; // Límite mínimo (70%)
          actualizarFuentes(scene.getRoot(), escalaActual);
          event.consume();
        }
        // Ctrl + "0" (Resetear)
        else if (event.getCode() == KeyCode.DIGIT0 ||
            event.getCode() == KeyCode.NUMPAD0) {

          escalaActual = 1.0;
          actualizarFuentes(scene.getRoot(), escalaActual);
          event.consume();
        }
      }
    });
    // ========================================================

    primaryStage.setTitle("Sistema de Reservas - Dashboard");
    primaryStage.setScene(scene);
    primaryStage.setMaximized(true);
    primaryStage.show();
  }

  /**
   * Recorre todo el árbol de componentes y les aplica el zoom.
   */
  private void actualizarFuentes(Node root, double escala) {
    // Verificamos si el nodo tiene propiedad de fuente (Labeled, TextInputControl,
    // Text)
    if (root instanceof Labeled || root instanceof TextInputControl || root instanceof Text) {

      // Guardamos el tamaño original la primera vez que pasa por acá
      Double baseSize = (Double) root.getProperties().get("baseFontSize");
      if (baseSize == null) {
        double currentSize = 13.0; // Tamaño por defecto si no encuentra fuente

        // Casteamos a la clase específica para usar getFont() sin errores
        if (root instanceof Labeled) {
          currentSize = ((Labeled) root).getFont().getSize();
        } else if (root instanceof TextInputControl) {
          currentSize = ((TextInputControl) root).getFont().getSize();
        } else if (root instanceof Text) {
          currentSize = ((Text) root).getFont().getSize();
        }

        if (currentSize <= 0)
          currentSize = 13.0;
        baseSize = currentSize;
        root.getProperties().put("baseFontSize", baseSize);
      }

      // Aplicamos el tamaño original multiplicado por la escala
      root.setStyle("-fx-font-size: " + (baseSize * escala) + "px;");
    }
    // Para otros componentes como Tablas, Menús o Paneles de pestañas, aplicamos un
    // tamaño base fijo
    else if (root instanceof TableView || root instanceof MenuBar || root instanceof TabPane) {
      Double baseSize = (Double) root.getProperties().get("baseFontSize");
      if (baseSize == null) {
        baseSize = 13.0;
        root.getProperties().put("baseFontSize", baseSize);
      }
      root.setStyle("-fx-font-size: " + (baseSize * escala) + "px;");
    }

    // Si el nodo es un contenedor (VBox, HBox, etc), entramos a sus hijos
    if (root instanceof Parent) {
      for (Node child : ((Parent) root).getChildrenUnmodifiable()) {
        actualizarFuentes(child, escala);
      }
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
