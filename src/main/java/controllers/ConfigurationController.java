package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.StyleManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ConfigurationController {

  @FXML
  private Button btnProducts;
  @FXML
  private Button btnServices;
  @FXML
  private Button btnManual;

  @FXML
  public void initialize() {
    btnProducts.setOnAction(e -> openProducts());
    btnServices.setOnAction(e -> openServices());
    btnManual.setOnAction(e -> openUserManual());
  }

  // =========================================================
  // Manual de usuario (PDF)
  // =========================================================
  private void openUserManual() {
    final String resourcePath = "/docs/ManualUsuario.pdf";

    // Todo el trabajo pesado (copiar el PDF + lanzar el visor)
    // va en un hilo aparte para no bloquear la UI.
    Thread worker = new Thread(() -> {
      try (InputStream in = getClass().getResourceAsStream(resourcePath)) {

        if (in == null) {
          Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
              "Manual no encontrado",
              "No se encontró el archivo del manual en los recursos de la aplicación.\n\n"
                  + "Verificá que exista: " + resourcePath));
          return;
        }

        Path tempFile = Files.createTempFile("Manual-Viridilnn-", ".pdf");
        Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        tempFile.toFile().deleteOnExit();

        openInSystemViewer(tempFile.toFile());

      } catch (IOException e) {
        e.printStackTrace();
        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
            "Error al abrir el manual",
            "No se pudo abrir el manual:\n" + e.getMessage()));
      }
    }, "manual-opener");

    worker.setDaemon(true);
    worker.start();
  }

  /**
   * Abre un archivo local con la aplicación por defecto del SO.
   *
   * Notas:
   * - En Linux, Desktop.open() suele bloquearse con Wayland o cuando
   * no hay un visor XDG registrado. Por eso preferimos xdg-open
   * directamente con ProcessBuilder y redirecciones a DISCARD,
   * sin esperar a que el proceso hijo termine.
   * - Este método se invoca SIEMPRE desde un hilo de fondo (no FX).
   */
  private void openInSystemViewer(File file) throws IOException {
    String os = System.getProperty("os.name").toLowerCase();
    String path = file.getAbsolutePath();

    ProcessBuilder pb;
    if (os.contains("win")) {
      pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", path);
    } else if (os.contains("mac")) {
      pb = new ProcessBuilder("open", path);
    } else {
      pb = new ProcessBuilder("xdg-open", path);
    }

    // No bloqueamos esperando al visor y no acumulamos I/O del hijo.
    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
    pb.start(); // NO waitFor()
  }

  // =========================================================
  // Productos
  // =========================================================
  private void openProducts() {
    try {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/views/Product.fxml"));

      Stage stage = new Stage();
      stage.setScene(new Scene(loader.load()));
      StyleManager.applyStyles(stage);

      stage.initOwner(btnProducts.getScene().getWindow());
      stage.initModality(Modality.WINDOW_MODAL);
      stage.setTitle("Productos");
      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // =========================================================
  // Servicios
  // =========================================================
  private void openServices() {
    try {
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/views/Service.fxml"));

      Stage stage = new Stage();
      stage.setScene(new Scene(loader.load()));
      StyleManager.applyStyles(stage);

      stage.initOwner(btnServices.getScene().getWindow());
      stage.initModality(Modality.WINDOW_MODAL);
      stage.setTitle("Servicios");
      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // =========================================================
  // Helper
  // =========================================================
  private void showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    StyleManager.applyStyles(alert.getDialogPane());
    alert.showAndWait();
  }
}
