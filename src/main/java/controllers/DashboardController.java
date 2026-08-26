package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class DashboardController {

    @FXML
    private VBox leftMenu;
    @FXML
    private AnchorPane centerPane;

    @FXML
    private ToggleButton btnPlanilla;
    @FXML
    private ToggleButton btnReservas;
    @FXML
    private ToggleButton btnHabitaciones;
    @FXML
    private ToggleButton btnClientes;
    @FXML
    private ToggleButton btnReportes;
    @FXML
    private ToggleButton btnConfiguracion;

    @FXML
    public void initialize() {
        System.out.println("DashboardController inicializado");

        btnPlanilla.setOnAction(e -> loadView("/views/PlanillaReserva.fxml"));
        btnReservas.setOnAction(e -> loadView("/views/Reservas.fxml"));
        btnHabitaciones.setOnAction(e -> loadView("/views/Room.fxml")); // corregido
        btnClientes.setOnAction(e -> loadView("/views/Clientes.fxml"));
        btnReportes.setOnAction(e -> loadView("/views/Reportes.fxml"));
        btnConfiguracion.setOnAction(e -> loadView("/views/Configuracion.fxml"));

    }

    private void loadView(String fxmlPath) {
        System.out.println("Cargando vista: " + fxmlPath);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane view = loader.load();
            centerPane.getChildren().setAll(view);
            System.out.println("Vista cargada correctamente");
        } catch (IOException e) {
            System.err.println("Error al cargar " + fxmlPath);
            e.printStackTrace();
            AnchorPane errorPane = new AnchorPane();
            centerPane.getChildren().setAll(errorPane);
        }
    }
}
