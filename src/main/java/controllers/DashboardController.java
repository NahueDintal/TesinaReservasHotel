package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

import javafx.scene.Parent;

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
        // Asignar acciones a los botones
        btnPlanilla.setOnAction(e -> loadView("/views/.fxml"));
        btnReservas.setOnAction(e -> loadView("/views/Reservations.fxml"));
        btnHabitaciones.setOnAction(e -> loadView("/views/Room.fxml"));
        btnClientes.setOnAction(e -> loadView("/views/CustomerView.fxml"));
        btnReportes.setOnAction(e -> loadView("/views/.fxml"));
        btnConfiguracion.setOnAction(e -> loadView("/views/.fxml"));

    }

    /**
     * Carga un archivo FXML en el panel central.
     *
     * @param fxmlPath Ruta del archivo FXML (debe comenzar con "/" y estar en
     *                 resources/views)
     */
    public void loadView(String fxmlPath) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();

        Object controller = loader.getController();
          if (controller instanceof RoomController) {
              ((RoomController) controller).setDashboardController(this);
          } else if (controller instanceof RoomFormController) {
              ((RoomFormController) controller).setDashboardController(this);
          } else if (controller instanceof ReservationsController) {

              ((ReservationsController) controller)
                      .setDashboardController(this);
          }
          centerPane.getChildren().setAll(view);
      } catch (IOException e) {
        e.printStackTrace();
        AnchorPane errorPane = new AnchorPane();
        centerPane.getChildren().setAll(errorPane);
      }
    }
}
