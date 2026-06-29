import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        DashboardController dashboard = new DashboardController();
        root.setTop(dashboard.getStatsPanel());
        root.setCenter(dashboard.getMonthlyGrid());

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Sistema de Reservas - Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
