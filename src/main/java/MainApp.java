import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
  public void start(Stage stage) throws Exception {
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