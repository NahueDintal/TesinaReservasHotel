package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import models.Reservation;
import javafx.scene.Parent;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import javafx.scene.control.Button;


public class DashboardController {

  @FXML
  private VBox leftMenu;

  @FXML
  private Button btnMenu;

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
  private ToggleButton btnPersonal;
  @FXML
  private ToggleButton btnConfiguracion;

  private ToggleGroup menuGroup;

  @FXML
  public void initialize() {
    // 1. Crear un ToggleGroup para que solo un botón esté seleccionado a la vez
    menuGroup = new ToggleGroup();
    btnPlanilla.setToggleGroup(menuGroup);
    loadView("/views/BookingChart.fxml");
    btnReservas.setToggleGroup(menuGroup);
    btnHabitaciones.setToggleGroup(menuGroup);
    btnClientes.setToggleGroup(menuGroup);
    btnReportes.setToggleGroup(menuGroup);
    btnPersonal.setToggleGroup(menuGroup);
    btnConfiguracion.setToggleGroup(menuGroup);

    // 2. Seleccionar "Planilla" por defecto
    btnPlanilla.setSelected(true);

    // 3. Asignar acciones a los botones
    btnPlanilla.setOnAction(e -> {
      selectButton(btnPlanilla);
      loadView("/views/BookingChart.fxml");
    });
    btnReservas.setOnAction(e -> {
      selectButton(btnReservas);
      loadView("/views/reservations.fxml");
    });
    btnHabitaciones.setOnAction(e -> {
      selectButton(btnHabitaciones);
      loadView("/views/RoomView.fxml");
    });
    btnClientes.setOnAction(e -> {
      selectButton(btnClientes);
      loadView("/views/CustomerView.fxml");
    });
    btnReportes.setOnAction(e -> {
      selectButton(btnReportes);
      loadView("/views/Report.fxml");
    });
    btnPersonal.setOnAction(e -> {
      selectButton(btnPersonal);
      loadView("/views/StaffView.fxml");
    });
    btnConfiguracion.setOnAction(e -> {
      selectButton(btnConfiguracion);
      loadView("/views/Configuration.fxml");
    });
  }

  @FXML
  private void toggleSidebar() {

    boolean visible = leftMenu.isVisible();

    leftMenu.setVisible(!visible);
    leftMenu.setManaged(!visible);
  }

  /**
   * Selecciona un botón y deselecciona los demás (a través del ToggleGroup)
   */
  private void selectButton(ToggleButton button) {
    button.setSelected(true);
  }

  /**
   * Carga un archivo FXML en el panel central.
   */
  public void loadView(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent view = loader.load();

      Object controller = loader.getController();

      if (controller instanceof ReservationsController) {
        ((ReservationsController) controller).setDashboardController(this);
      }
      if (controller instanceof NewReservationController) {
        ((NewReservationController) controller).setDashboardController(this);
      }
      if (controller instanceof CustomerController) { // ← NUEVO
        ((CustomerController) controller).setDashboardController(this);
      }

      centerPane.getChildren().clear();
      centerPane.getChildren().add(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {
      e.printStackTrace();
      centerPane.getChildren().setAll(new AnchorPane());
    }
  }

  public void loadEditReservation(Reservation reservation) {
    selectSidebarButton("reservas");
    try {

      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("/views/NewReservation.fxml"));

      Parent view = loader.load();

      NewReservationController controller = loader.getController();

      controller.setDashboardController(this);
      controller.setReservationToEdit(reservation);

      // Colocar la vista exactamente igual que las demás
      centerPane.getChildren().setAll(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadReservationConsumptions(Reservation reservation) {
    selectSidebarButton("reservas");
    try {

      FXMLLoader loader = new FXMLLoader(
              getClass().getResource(
                      "/views/ConsumptionManagement.fxml"
              )
      );

      Parent view = loader.load();

      ConsumptionManagementController controller =
              loader.getController();

      controller.setDashboardController(this);
      controller.setReservation(reservation);

      centerPane.getChildren().setAll(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {

      e.printStackTrace();
    }
  }

  public void loadReservationDetail(Reservation reservation) {
    selectSidebarButton("reservas");
    try {

      FXMLLoader loader = new FXMLLoader(
              getClass().getResource(
                      "/views/ReservationDetail.fxml"
              )
      );

      Parent view = loader.load();

      ReservationDetailController controller =
              loader.getController();

      controller.setDashboardController(this);
      controller.setReservation(reservation);

      centerPane.getChildren().setAll(view);

      AnchorPane.setTopAnchor(view, 0.0);
      AnchorPane.setBottomAnchor(view, 0.0);
      AnchorPane.setLeftAnchor(view, 0.0);
      AnchorPane.setRightAnchor(view, 0.0);

    } catch (IOException e) {

      e.printStackTrace();
    }
  }
  /**
   * Selecciona programáticamente un botón de la sidebar.
   * Útil cuando se navega a una vista desde otra (ej: historial → reservas).
   */
  public void selectSidebarButton(String buttonName) {
    switch (buttonName) {
      case "reservas":
        selectButton(btnReservas);
        break;
      case "habitaciones":
        selectButton(btnHabitaciones);
        break;
      case "clientes":
        selectButton(btnClientes);
        break;
      case "planilla":
        selectButton(btnPlanilla);
        break;
      case "reportes":
        selectButton(btnReportes);
        break;
      case "personal":
        selectButton(btnPersonal);
        break;
      case "configuracion":
        selectButton(btnConfiguracion);
        break;
      default:
        System.err.println("Botón desconocido: " + buttonName);
    }
  }

}
