import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
<<<<<<< HEAD
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Dashboard.fxml"));
    BorderPane root = loader.load();
    Scene scene = new Scene(root, 1200, 800);
    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
    primaryStage.setTitle("Sistema de Reservas - Dashboard");
    primaryStage.setScene(scene);
    primaryStage.show();
=======
  public void start(Stage stage) throws Exception {
    // ===== CARGA DEL FXML (tu código original) =====
    FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/views/CustomerView.fxml")
    );

    Scene scene = new Scene(loader.load());
    stage.setTitle("Gestión de Clientes - Hotel");
    stage.setScene(scene);
    stage.show();
>>>>>>> b30a62b (ScriptsSQL Actualizados || mainApp limpiado || archivos relacionados a Customer Actualizados)
  }

  public static void main(String[] args) {
    launch(args);
  }
}