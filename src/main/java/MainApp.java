import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    // ===== CÓDIGO DE PRUEBA (lo agregás acá) =====
    System.out.println("=== DIAGNÓSTICO DE RUTA FXML ===");

    // 1. Verificar si el archivo existe en el classpath
    java.net.URL fxmlUrl = getClass().getResource("/views/CustomerView.fxml");
    System.out.println("¿Existe el FXML? " + (fxmlUrl != null));

    if (fxmlUrl != null) {
      System.out.println("Ruta encontrada: " + fxmlUrl);
    } else {
      System.err.println("❌ NO se encontró el FXML en /views/CustomerView.fxml");

      // 2. Mostrar todas las rutas que SÍ existen (para depurar)
      System.out.println("Contenido de /views/: " +
              getClass().getResource("/views/"));

      // 3. Ver si el archivo está en otro lado
      System.out.println("¿En /fxml/? " +
              (getClass().getResource("/fxml/CustomerView.fxml") != null));
      System.out.println("¿En /CustomerView.fxml? " +
              (getClass().getResource("/CustomerView.fxml") != null));
      System.out.println("¿En views/CustomerView.fxml? " +
              (getClass().getResource("views/CustomerView.fxml") != null));
    }
    System.out.println("==================================");

    // ===== CARGA DEL FXML (tu código original) =====
    FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/views/CustomerView.fxml")
    );

    Scene scene = new Scene(loader.load());
    stage.setTitle("Gestión de Clientes - Hotel");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}