package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class DashboardController {

  @FXML
  private VBox leftMenu; // Referencia al panel izquierdo (opcional)
  @FXML
  private AnchorPane centerPane; // Área central donde se cargarán las vistas

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
    // Asignar acciones a cada botón
    btnPlanilla.setOnAction(e -> loadView("/views/PlanillaReserva.fxml"));
    btnReservas.setOnAction(e -> loadView("/views/Reservas.fxml"));
    btnHabitaciones.setOnAction(e -> loadView("/views/Habitaciones.fxml"));
    btnClientes.setOnAction(e -> loadView("/views/Clientes.fxml"));
    btnReportes.setOnAction(e -> loadView("/views/Reportes.fxml"));
    btnConfiguracion.setOnAction(e -> loadView("/views/Configuracion.fxml"));

    // Cargar una vista por defecto (Planilla de reserva)
    loadView("/views/PlanillaReserva.fxml");
  }

  /**
   * Carga un FXML en el AnchorPane central.
   * 
   * @param fxmlPath Ruta del archivo FXML (debe comenzar con "/" y estar en
   *                 resources/views)
   */
  private void loadView(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      AnchorPane view = loader.load();
      // Reemplazar el contenido del centro
      centerPane.getChildren().setAll(view);
    } catch (IOException e) {
      e.printStackTrace();
      // Si falla, mostrar un mensaje de error en el centro
      AnchorPane errorPane = new AnchorPane();
      // Puedes agregar un Label con el error, pero por simplicidad lo dejamos vacío
      centerPane.getChildren().setAll(errorPane);
    }
  }
}
